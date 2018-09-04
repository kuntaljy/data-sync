package com.dist.datasync.config.service;

import com.dist.datasync.config.dto.FieldInfo;
import com.dist.datasync.config.dto.SequenceInfo;
import com.dist.datasync.config.dto.TableInfo;
import com.dist.datasync.config.entity.DataSourceConfig;
import com.dist.datasync.config.entity.FieldConfig;
import com.dist.datasync.config.entity.SequenceConfig;
import com.dist.datasync.config.entity.TableConfig;
import com.dist.datasync.config.repository.DataSourceConfigRepository;
import com.dist.datasync.config.repository.FieldConfigRepository;
import com.dist.datasync.config.repository.SequenceConfigRepository;
import com.dist.datasync.config.repository.TableConfigRepository;
import javafx.beans.property.adapter.ReadOnlyJavaBeanBooleanProperty;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import javax.sql.DataSource;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 获取数据库表、字段、序列配置
 * @author lijy
 */
@Service
public class DBConfigService {

    @Autowired
    DataSourceService dataSourceService;

    @Autowired
    DataSourceConfigRepository dataSourceConfigRepository;

    @Autowired
    TableConfigRepository tableConfigRepository;

    @Autowired
    FieldConfigRepository fieldConfigRepository;

    @Autowired
    SequenceConfigRepository sequenceConfigRepository;

    /**
     * 从数据源获取所有用户表
     * @return
     */
    public List<TableInfo> getAllTables(Integer dataSourceConfigId){
        List<TableInfo> tableInfoList = new ArrayList<>();

        //查询数据源配置
        DataSourceConfig config = dataSourceConfigRepository.findById(dataSourceConfigId).orElse(null);
        if(config == null){
            return null;
        }

        //读取数据库中的所有的表名
        DataSource dataSource = dataSourceService.getDataSource(config);
        JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
        String sql = "select table_name from user_tables";
        List<Map<String,Object>> tableList = jdbcTemplate.queryForList(sql);
        for(Map<String,Object> table: tableList){
            TableInfo tableInfo = new TableInfo();
            tableInfo.setTableName(table.get("TABLE_NAME").toString());
            tableInfoList.add(tableInfo);
        }

        return tableInfoList;
    }


    /**
     * 从数据源获取所有用户表，并与已经配置过的序列做对比，给已配置的项Id赋值
     * @return
     */
    public List<TableInfo> getAllTables(Integer dataSourceConfigId, Integer syncTaskConfigId){
        List<TableInfo> tableInfoList = getAllTables(dataSourceConfigId);
        List<TableConfig> tableConfigList = tableConfigRepository.findByTaskId(syncTaskConfigId);
        for(TableInfo tableInfo: tableInfoList){
            for(TableConfig tableConfig: tableConfigList){
                //匹配成功，将勾选状态设置为true
                //并给Id赋值，方便调用方使用
                if(tableInfo.getTableName().equals(tableConfig.getTableName())){
                    tableInfo.setId(tableConfig.getId());
                    tableInfo.setTaskId(syncTaskConfigId);
                    tableInfo.setSelected(true);
                    break;
                }
            }
        }
        return tableInfoList;
    }

    /**
     * 从数据源获取所有用户表及其字段
     * @return
     */
    public List<FieldInfo> getFieldsOfTable(Integer dataSourceConfigId, String tableName){
        List<FieldInfo> fieldInfoList = new ArrayList<>();

        //查询数据源配置
        DataSourceConfig config = dataSourceConfigRepository.findById(dataSourceConfigId).orElse(null);
        if(config == null){
            return null;
        }

        //读取数据库中的所有的表名
        DataSource dataSource = dataSourceService.getDataSource(config);
        JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
        String sql = String.format("select * from user_tab_columns where table_name = '%s'", tableName.toUpperCase());
        List<Map<String,Object>> fieldList = jdbcTemplate.queryForList(sql);
        for(Map<String,Object> field: fieldList){
            FieldInfo fieldInfo = new FieldInfo();
            fieldInfo.setName(field.get("COLUMN_NAME").toString());
            fieldInfo.setDataType(field.get("DATA_TYPE").toString());
            fieldInfo.setLength(((BigDecimal) field.get("CHAR_LENGTH")).longValue());
            Object percision = field.get("DATA_PERCICION");
            if(percision!=null) {
                fieldInfo.setPercision(((BigDecimal) field.get("DATA_PERCICION")).longValue());
            }
            fieldInfo.setSelected(false);
            fieldInfoList.add(fieldInfo);
        }
        return fieldInfoList;
    }


    /**
     * 从数据源获取指定表的所有字段，并与配置过的字段进行对比，将已经配置的项设置勾选状态为true
     * @return
     */
    public List<FieldInfo> getFieldsOfTable(Integer dataSourceConfigId, String tableName, Integer tableId){
        List<FieldInfo> fieldInfoList = getFieldsOfTable(dataSourceConfigId, tableName);
        List<FieldConfig> fieldConfigList = fieldConfigRepository.findByTableId(tableId);
        for (FieldInfo fieldInfo:fieldInfoList){
            for(FieldConfig fieldConfig: fieldConfigList){
                //匹配成功，设置勾选状态为true
                //并给Id赋值，便于客户端处理
                if(fieldInfo.getName().equals(fieldConfig.getName())){
                    fieldInfo.setId(fieldConfig.getId());
                    fieldInfo.setTableId(tableId);
                    fieldInfo.setSelected(true);
                    break;
                }
            }
        }
        return fieldInfoList;
    }

    /**
     * 从数据源获取所有序列
     * @param dataSourceConfigId
     * @return
     */
    public List<SequenceInfo> getAllSequences(Integer dataSourceConfigId){
        List<SequenceInfo> sequenceInfoList = new ArrayList<>();

        //查询数据源配置
        DataSourceConfig config = dataSourceConfigRepository.findById(dataSourceConfigId).orElse(null);
        if(config == null){
            return null;
        }

        //读取数据库中的所有的序列名
        DataSource dataSource = dataSourceService.getDataSource(config);
        JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
        String sql = "select sequence_name from user_sequences";
        List<Map<String,Object>> sequenceList = jdbcTemplate.queryForList(sql);
        for(Map<String,Object> sequence: sequenceList){
            SequenceInfo sequenceInfo = new SequenceInfo();
            sequenceInfo.setSequenceName(sequence.get("SEQUENCE_NAME").toString());
            sequenceInfoList.add(sequenceInfo);
        }

        return sequenceInfoList;
    }

    /**
     * 从数据源获取所有序列，并与已经配置过的序列做对比，将已配置的项设置勾选状态为true
     * @param dataSourceConfigId
     * @return
     */
    public List<SequenceInfo> getAllSequences(Integer dataSourceConfigId, Integer syncTaskConfigId){
        List<SequenceInfo> sequenceInfoList = getAllSequences(dataSourceConfigId);
        List<SequenceConfig> sequenceConfigList = sequenceConfigRepository.findByTaskId(syncTaskConfigId);
        for (SequenceInfo sequenceInfo:sequenceInfoList){
            for(SequenceConfig sequenceConfig:sequenceConfigList){
                //匹配成功，设置勾选状态为true
                //并给Id赋值，便于前端处理
                if(sequenceInfo.getSequenceName().equals(sequenceConfig.getSequenceName())){
                    sequenceInfo.setId(sequenceConfig.getId());
                    sequenceInfo.setTaskId(syncTaskConfigId);
                    sequenceInfo.setSelected(true);
                    break;
                }
            }
        }
        return sequenceInfoList;
    }
}
