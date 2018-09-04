package com.dist.datasync.base;

import com.dist.datasync.config.entity.FieldConfig;

import java.util.List;

/**
 * @author lijy
 */
public class SqlHelper {

    /**
     * 表名最长不超过30个字符
     * @param tableName
     * @return
     */
    public static String getTempTableName(String tableName){
        String tmpTalbeName = "tmp_" + tableName;
        if(tmpTalbeName.length()>=30) {
            tmpTalbeName = tmpTalbeName.substring(0,30);
        }
        return tmpTalbeName;
    }

    /***
     * 参考指定表创建一个相同结构但数据为空的临时表的sql
     * @param tableName
     * @return
     */
    public static String buildTempTableSql(String tableName){
        String tmpTableName = getTempTableName(tableName);
        /*
        数据库本身的临时表在批示处理期间存在，表丢失问题，因此改用实体表充当临时表
        String sql_tmp = String.format("create global temporary table %s on commit preserve rows " +
                "as select * from %s where 1 = -1", tmpTableName, tableName);
        */
        String sql_tmp = String.format("create table %s as select * from %s where 1 = -1", tmpTableName, tableName);
        return sql_tmp;
    }

    /***
     * 构造查询语句
     * @param tableName
     * @param fields
     * @return
     */
    public static String buildQuerySql(String tableName, List<String> fields){
        String fieldStr = String.join(",",fields.toArray(new String[fields.size()]));
        String query = String.format("select %s from %s", fieldStr, tableName);
        return query;
    }


    /***
     * 构造插入语句
     * @param tableName
     * @param fields
     * @return
     */
    public static String buildInsertSql(String tableName, List<String> fields) {
        //构造格式：insert into table(col1,col2,col3) values(?,?,?)
        String fieldStr = String.join(",", fields.toArray(new String[fields.size()]));
        StringBuilder sqlBuilder = new StringBuilder(String.format("insert into %s(%s) values(", tableName, fieldStr));
        for (int i = 0; i < fields.size() - 1; i++) {
            sqlBuilder.append("?,");
        }
        sqlBuilder.append("?)");
        return sqlBuilder.toString();
    }

    /***
     * 构造merge语句
     * @param tableName
     * @param fields
     * @return
     */
    public static String buildMergeSql(String tableName, List<String> fields){
        //格式说明：MERGE INTO table t
        //        USING (select * from tmp_talbe) s
        //        ON (t.id = s.id)
        //        WHEN MATCHED THEN
        //        UPDATE  SET t.col1 = s.col1, t.col2 = s.col2
        //        WHEN NOT MATCHED THEN
        //        INSERT (t.col1, t.col2) VALUES (s.col1, s.col2);

        String tmpTableName = getTempTableName(tableName);

        StringBuilder sqlBuilder = new StringBuilder("merge into ").append(tableName).append(" t ");
        sqlBuilder.append(" using ( select * from ").append(tmpTableName).append(") s");
        sqlBuilder.append(" on (t.id = s.id)");
        sqlBuilder.append(" when matched then update set ");
        for(String f: fields) {
            if ("id".equals(f.toLowerCase())) continue;//跳过id字段
            sqlBuilder.append("t.").append(f).append("=s.").append(f).append(",");
        }
        sqlBuilder.delete(sqlBuilder.length()-1, sqlBuilder.length());
        sqlBuilder.append(" when not matched then insert (");
        for(String f: fields){
            sqlBuilder.append("t.").append(f).append(",");
        }
        sqlBuilder.delete(sqlBuilder.length()-1, sqlBuilder.length());
        sqlBuilder.append(") values (");
        for(String f: fields){
            sqlBuilder.append("s.").append(f).append(",");
        }
        sqlBuilder.delete(sqlBuilder.length()-1, sqlBuilder.length());
        sqlBuilder.append(")");
        return sqlBuilder.toString();
    }

    /***
     * 构造删除语句
     * @param tableName
     * @return
     */
    public static String buildDeleteSql(String tableName){
        String tmpTableName = getTempTableName(tableName);
        String sql = String.format("delete from %s t where t.id not in(select id from %s)", tableName, tmpTableName);
        return sql;
    }



    /***
     * 构造删除语句
     * @param tableName
     * @return
     */
    public static String sql4FKName(String tableName){
        return String.format("select t.constraint_name from user_constraints t where t.constraint_type = 'R' and t.table_name = '%s'", tableName);
    }
}
