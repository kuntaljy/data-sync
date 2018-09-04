package com.dist.datasync.engine;

import com.dist.datasync.base.Result;
import com.dist.datasync.config.entity.SequenceConfig;
import com.dist.datasync.config.dto.SyncTaskModel;
import com.dist.datasync.config.entity.TableConfig;
import com.dist.datasync.base.SqlHelper;
import oracle.sql.BLOB;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.jdbc.datasource.DataSourceUtils;
import org.springframework.jdbc.support.JdbcUtils;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.sql.*;

/**
 * 根据同步配置信息，进行数据库同步。
 * 前提是源库与目标库表结构完全一致，每个表都有一个ID字段作为主键
 * 1. 首先检查同步条件，如果目标库无此表则不同步，如果源表无数据也不同步
 * 2. 首先在目标库创建一张与目标表相同结构的临时表
 * 3. 将源数据插入到临时表中，
 * 4. 再通过oracle的merge语句实现插入更新
 * 5. 然后删除目标表比临时表多出的记录，到此数据同步已经全部完成
 * 6. 最后清除临时表
 * @author lijy
 */
@Scope("prototype")
@Component("simpleSyncHandler")
public class SimpleSyncHandler implements SyncHandler {

    private Integer batchCount = 100;
    protected static Logger logger = LoggerFactory.getLogger(SimpleSyncHandler.class);

    @Override
    public void excute(SyncTaskModel syncTaskModel, ProgressCallBack progressCallBack, FinishCallBack finishCallBack){
        //已经处理的对象计数和需要处理的对象总数，用于计算处理进度
        Float processedCount = 0f;
        Integer objectCount = syncTaskModel.getTableConfigs().size() + syncTaskModel.getSequenceConfigs().size();
        //统计错误次数
        Integer exceptionCount = 0;

        logger.info("开始同步");
        Connection sourceConn = null;
        Connection targetConn = null;
        try {
            logger.info("建立数据库连接");
            sourceConn = syncTaskModel.getSource().getConnection();
            targetConn = syncTaskModel.getTarget().getConnection();
            targetConn.setAutoCommit(false);//设置为手动提交

            for (TableConfig tableConfig : syncTaskModel.getTableConfigs()) {
                //进度回调
                progressCallBack.setProgress(processedCount++/objectCount);
                //得到表名
                String tableName = tableConfig.getTableName();
                String tmpTable = SqlHelper.getTempTableName(tableName);
                logger.info("开始处理表：" + tableName);
                //1.同步前检查
                if(!check(sourceConn, targetConn, tableConfig)) {
                    exceptionCount++;
                    continue;
                }
                //2.创建临时表
                if (!createTempTable(tableName, targetConn)){
                    exceptionCount++;
                    continue;
                }
                //3.先将数据存入临时表中
                if (!copy2TempTable(sourceConn, targetConn, tableConfig)){
                    clearAndDropTable(tmpTable, targetConn);
                    exceptionCount++;
                    continue;
                }
                //4.然后使用oracle的merge语句实现插入与更新操作
                if (!insertUpdate(tableConfig, targetConn)){
                    clearAndDropTable(tmpTable, targetConn);
                    exceptionCount++;
                    continue;
                }
                //5.接下来删除多余的数据
                if (!deleteDate(tableConfig, targetConn)){
                    clearAndDropTable(tmpTable, targetConn);
                    exceptionCount++;
                    continue;
                }
                //6.最后清空并drop临时表
                if(!clearAndDropTable(tmpTable, targetConn)){
                    exceptionCount++;
                    continue;
                }
            }

            for(SequenceConfig sequenceConfig: syncTaskModel.getSequenceConfigs()){
                //进度回调
                progressCallBack.setProgress(processedCount++/objectCount);
                //得到序列名
                String sequenceName = sequenceConfig.getSequenceName();
                logger.info("开始处理序列：" + sequenceName);
                //1.检查序列是否存在
                if(!sequenceExists(targetConn, sequenceName)){
                    exceptionCount++;
                    continue;
                }
                //2.设置序列当前值
                if(!updateSequence(sourceConn, targetConn, sequenceName)){
                    exceptionCount++;
                    continue;
                }
            }
            //进度回调
            progressCallBack.setProgress(processedCount++/objectCount);
            logger.info("处理完成，出现异常数：" + exceptionCount);
            finishCallBack.finish(exceptionCount, true, "处理完成");
        }catch (Exception e) {
            logger.error("数据同步失败",e);
            finishCallBack.finish(exceptionCount, false,"同步失败！" + e.getMessage());
        }finally {
            DataSourceUtils.releaseConnection(sourceConn, syncTaskModel.getSource());
            DataSourceUtils.releaseConnection(targetConn, syncTaskModel.getTarget());
        }
    }


    /***
     * 同步前检查
     * @param sourceConn
     * @param targetConn
     * @param tableConfig
     * @return
     */
    protected boolean check(Connection sourceConn, Connection targetConn, TableConfig tableConfig){
        logger.info("开始执行同步前检查");
        //先检查目标库中是否存在这个表
        boolean checkResult = tableExists(targetConn, tableConfig.getTableName());
        if(!checkResult){
            logger.warn("目标库中不存在表：" + tableConfig.getTableName());
        }
        return checkResult;
    }


    /***
     * 检查表是否存在
     * @param conn
     * @param tableName
     * @return
     */
    protected boolean tableExists(Connection conn, String tableName){
        logger.info("检查表是否存在");
        boolean checkResult = false;
        Statement stmt = null;
        ResultSet resultSet = null;
        try {
            String sql_exists = String.format("select count(*) from user_tables t where table_name = upper('%s')", tableName);
            logger.info(sql_exists);
            stmt = conn.createStatement();
            resultSet = stmt.executeQuery(sql_exists);
            if(resultSet.next()){
                checkResult = resultSet.getInt(1) > 0;
            }
            if(!checkResult) {
                logger.info("库中不存在表:" + tableName);
                return false;
            }
            logger.info("库中存在表:" + tableName);
            return true;
        } catch (SQLException e) {
            logger.error("检查失败", e);
            return false;
        }finally {
            JdbcUtils.closeResultSet(resultSet);
            JdbcUtils.closeStatement(stmt);
        }
    }

    /***
     * 检查表里面是否有数据
     * @param conn
     * @param tableName
     * @return
     */
    protected boolean tableHasData(Connection conn, String tableName){
        logger.info("检查表里面是否有数据");
        boolean checkResult = false;
        Statement stmt = null;
        ResultSet resultSet = null;
        try {
            String sql_data = String.format("select count(*) from %s", tableName);
            logger.info(sql_data);
            stmt = conn.createStatement();
            resultSet = stmt.executeQuery(sql_data);
            if(resultSet.next()){
                checkResult = resultSet.getInt(1) > 0;
            }
            if(!checkResult) {
                logger.warn("表中无数据：" + tableName);
                return false;
            }
            logger.info("表中有数据：" + tableName);
            return true;
        } catch (SQLException e) {
            logger.error("同步前检查", e);
            return false;
        }finally {
            JdbcUtils.closeResultSet(resultSet);
            JdbcUtils.closeStatement(stmt);
        }
    }

    /***
     * 创建临时表
     * @param tableName
     * @param connection
     * @return
     */
    protected boolean createTempTable(String tableName, Connection connection){
        logger.info("开始创建临时表");
        String tmpTableName = SqlHelper.getTempTableName(tableName);

        //先判断表是否存在，存在则清除
        boolean exists = tableExists(connection, tmpTableName);
        if(exists){
            clearAndDropTable(tmpTableName, connection);
        }

        //创建临时表
        String sql_create = SqlHelper.buildTempTableSql(tableName);
        Statement tmp_stmt = null;
        try {
            tmp_stmt = connection.createStatement();
            logger.info(sql_create);
            tmp_stmt.execute(sql_create);
            logger.info("创建临时表成功");
            return true;
        } catch (SQLException e) {
            logger.error("创建临时表失败", e);
            return false;
        }finally {
            JdbcUtils.closeStatement(tmp_stmt);
        }
    }

    /***
     * 将数据存入临时表中
     * @param sourceConn
     * @param targetConn
     * @param tableConfig
     * @return
     */
    protected boolean copy2TempTable(Connection sourceConn, Connection targetConn, TableConfig tableConfig){
        logger.info("开始将数据插入临时表");
        String tmpTableName = SqlHelper.getTempTableName(tableConfig.getTableName());
        String sql_query = SqlHelper.buildQuerySql(tableConfig.getTableName(), tableConfig.getSyncFieldArray());
        String sql_insert = SqlHelper.buildInsertSql(tmpTableName, tableConfig.getSyncFieldArray());
        PreparedStatement query_stmt = null;
        PreparedStatement insert_stmt = null;
        ResultSet resultSet = null;
        try {
            logger.info("查询源库数据");
            query_stmt =  sourceConn.prepareStatement(sql_query);
            resultSet = query_stmt.executeQuery();

            logger.info("组织批量插入语句");
            insert_stmt = targetConn.prepareStatement(sql_insert);
            Integer rowCount=0;
            while (resultSet.next()){
                rowCount++;
                for(int i=1; i<=tableConfig.getSyncFields().size(); i++) {
                    if(resultSet.getObject(i) instanceof Blob){
                        Blob blob = resultSet.getBlob(i);
                        InputStream inputStream = blob.getBinaryStream();
                        insert_stmt.setBlob(i, inputStream);
                    }else if(resultSet.getObject(i) instanceof Clob){
                        Clob clob = resultSet.getClob(i);
                        Reader reader = clob.getCharacterStream();
                        insert_stmt.setClob(i, reader);
                    }else {
                        insert_stmt.setObject(i, resultSet.getObject(i));
                    }
                }
                insert_stmt.addBatch();

                //达到批量提交的数据，则执行提交
                if(rowCount.equals(batchCount)){
                    logger.info("批量提交数据插入临时表，提交记录数：" + batchCount);
                    insert_stmt.executeBatch();
                    targetConn.commit();
                    rowCount=0;
                }
            }
            logger.info("批量提交剩余数据插入临时表");
            insert_stmt.executeBatch();
            targetConn.commit();
            logger.info("数据插入临时表成功");
            return true;
        } catch (SQLException e) {
            logger.error("批量插入临时表失败", e);
            return false;
        }finally {
            JdbcUtils.closeResultSet(resultSet);
            JdbcUtils.closeStatement(query_stmt);
            JdbcUtils.closeStatement(insert_stmt);
        }
    }

    /***
     * 利用merge语句执行插入更新
     * @param tableConfig
     * @param targetConn
     * @return
     */
    protected boolean insertUpdate(TableConfig tableConfig, Connection targetConn){
        logger.info("执行merge插入更新");
        String sql_merge = SqlHelper.buildMergeSql(tableConfig.getTableName(), tableConfig.getSyncFieldArray());
        Statement merge_stmt = null;
        try {
            merge_stmt = targetConn.createStatement();
            logger.info(sql_merge);
            merge_stmt.execute(sql_merge);
            targetConn.commit();
            logger.info("merge插入更新执行成功");
            return true;
        } catch (SQLException e) {
            logger.error("merge插入更新失败", e);
            return false;
        }finally {
            JdbcUtils.closeStatement(merge_stmt);
        }
    }

    /***
     * 对比临时表删除多余数据
     * @param tableConfig
     * @param targetConn
     * @return
     */
    protected boolean deleteDate(TableConfig tableConfig, Connection targetConn){
        logger.info("执行数据删除前先禁用外键约束");
        if(!foreignKeySwitch(targetConn,tableConfig.getTableName(),false)){
            return false;
        }

        //禁用外键后执行数据删除
        if(!doDeleteDate(tableConfig, targetConn)){
            return false;
        }

        logger.info("执行数据删除后启用外键约束");
        if(!foreignKeySwitch(targetConn,tableConfig.getTableName(),true)){
            return false;
        }

        return true;
    }

    /***
     * 对比临时表删除多余数据
     * @param tableConfig
     * @param targetConn
     * @return
     */
    protected boolean doDeleteDate(TableConfig tableConfig, Connection targetConn){
        logger.info("执行数据删除");
        String sql_delete = SqlHelper.buildDeleteSql(tableConfig.getTableName());
        Statement del_stmt = null;
        try {
            del_stmt = targetConn.createStatement();
            logger.info(sql_delete);
            del_stmt.execute(sql_delete);
            targetConn.commit();
            logger.info("数据删除成功");
            return true;
        } catch (SQLException e) {
            logger.error("数据删除失败", e);
            return false;
        }finally {
            JdbcUtils.closeStatement(del_stmt);
        }
    }

    /**
     * 启用或禁用指定表的外键约束
     * @param conn 数据库连接
     * @param tableName 表名
     * @param switchValue true启用，false禁用
     * @return
     */
    protected boolean foreignKeySwitch(Connection conn, String tableName, Boolean switchValue){
        String switchStr = switchValue?"启用":"禁用";
        String state = switchValue?"enable":"disable";
        logger.info(String.format("开始%s表%s的所有外键约束",switchStr, tableName));
        String sql_query = SqlHelper.sql4FKName(tableName);
        String sql_switch = "alter table %s %s constraint %s";

        Statement stmt_query = null;
        Statement stmt_switch = null;
        ResultSet resultSet = null;
        try {
            stmt_query = conn.createStatement();
            logger.info(sql_query);
            resultSet = stmt_query.executeQuery(sql_query);
            while (resultSet.next()){
                String fkName = resultSet.getString(1);
                String sql = String.format(sql_switch, tableName, state, fkName);
                logger.info(sql);
                stmt_switch = conn.createStatement();
                stmt_switch.execute(sql);
            }
            conn.commit();
            logger.info(String.format("外键%s完毕",switchStr));
            return true;
        } catch (SQLException e) {
            logger.error(String.format("外键%s失败",switchStr),e);
            return false;
        }finally {
            JdbcUtils.closeResultSet(resultSet);
            JdbcUtils.closeStatement(stmt_query);
            JdbcUtils.closeStatement(stmt_switch);
        }
    }

    /***
     * 清除临时表
     * @param tableName
     * @param targetConn
     * @return
     */
    protected boolean clearAndDropTable(String tableName, Connection targetConn){
        logger.info("开始清除表");
        String sql_truncate = "truncate table " + tableName;
        String sql_drop = "drop table " + tableName;

        Statement clear_stmt = null;
        try {
            clear_stmt = targetConn.createStatement();

            logger.info(sql_truncate);
            clear_stmt.execute(sql_truncate);
            targetConn.commit();

            logger.info(sql_drop);
            clear_stmt.execute(sql_drop);
            targetConn.commit();

            logger.info("表清除完毕");
            return true;
        } catch (SQLException e) {
            logger.error("清除表失败", e);
            return false;
        }finally {
            JdbcUtils.closeStatement(clear_stmt);
        }
    }


    /***
     * 检查序列是否存在
     * @param conn
     * @param sequenceName
     * @return
     */
    protected boolean sequenceExists(Connection conn, String sequenceName){
        logger.info("检查序列是否存在");
        boolean checkResult = false;
        Statement stmt = null;
        ResultSet resultSet = null;
        try {
            String sql_exists = String.format("select count(*) from user_sequences  t where sequence_name = upper('%s')", sequenceName);
            logger.info(sql_exists);
            stmt = conn.createStatement();
            resultSet = stmt.executeQuery(sql_exists);
            if(resultSet.next()){
                checkResult = resultSet.getInt(1) > 0;
            }
            if(!checkResult) {
                logger.warn("库中不存在序列:" + sequenceName);
                return false;
            }
            logger.info("库中存在序列:" + sequenceName);
            return true;
        } catch (SQLException e) {
            logger.error("检查失败", e);
            return false;
        }finally {
            JdbcUtils.closeResultSet(resultSet);
            JdbcUtils.closeStatement(stmt);
        }
    }

    protected boolean updateSequence(Connection sourceConn, Connection targetConn, String sequenceName){
        logger.info("检查序列当前值");
        logger.info("从源库获取序列值");
        Long srcSeqNextValue = getSeqNextValue(sourceConn, sequenceName);
        Long tgtSeqNextVlaue = getSeqNextValue(targetConn, sequenceName);
        if(srcSeqNextValue < 0 || tgtSeqNextVlaue < 0){
            logger.info("序列获取失败");
            return false;
        }
        if(srcSeqNextValue.equals(tgtSeqNextVlaue)){
            logger.info("序列值相同无需设置");
            return true;
        }

        logger.info("修改目标序列当前值");
        //先修改序列的increment值，然后获取下一个值，最后在把increment值还原为1
        Long increment = srcSeqNextValue - tgtSeqNextVlaue;
        setSeqIncrement(targetConn, sequenceName, increment);
        tgtSeqNextVlaue = getSeqNextValue(targetConn, sequenceName);
        setSeqIncrement(targetConn, sequenceName, 1L);

        if(srcSeqNextValue.equals(tgtSeqNextVlaue)){
            logger.info("序列修改成功");
            return true;
        }else {
            logger.warn("序列修改失败");
            return false;
        }
    }


    /**
     * 获取序列下一个值
     * @param conn
     * @param sequenceName
     * @return
     */
    protected Long getSeqNextValue(Connection conn, String sequenceName){
        logger.info("获取序列当前值");
        Statement stmt = null;
        ResultSet resultSet = null;
        try {
            String sql = String.format("select %s.nextval from dual", sequenceName);
            logger.info(sql);
            stmt = conn.createStatement();
            resultSet = stmt.executeQuery(sql);
            Long nextVal = -1L;
            if(resultSet.next()){
                nextVal = resultSet.getLong(1);
            }
            logger.info("序列值为:" + nextVal);
            return nextVal;
        }catch (SQLException e) {
            logger.error("检查失败", e);
            return -1L;
        }finally {
            JdbcUtils.closeResultSet(resultSet);
            JdbcUtils.closeStatement(stmt);
        }
    }

    /**
     * 获取序列下一个值
     * @param conn
     * @param sequenceName
     * @return
     */
    protected boolean setSeqIncrement(Connection conn, String sequenceName, Long increment){
        logger.info("设置序列增量值");
        Statement stmt = null;
        ResultSet resultSet = null;
        try {
            String sql = String.format("alter sequence %s increment by %s", sequenceName, increment.toString());
            logger.info(sql);
            stmt = conn.createStatement();
            stmt.execute(sql);
            conn.commit();
            logger.info("序列增量设置成功");
            return true;
        }catch (SQLException e) {
            logger.error("设置失败", e);
            return false;
        }finally {
            JdbcUtils.closeResultSet(resultSet);
            JdbcUtils.closeStatement(stmt);
        }
    }
}
