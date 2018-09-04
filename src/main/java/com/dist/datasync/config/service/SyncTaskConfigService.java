package com.dist.datasync.config.service;

import com.dist.datasync.config.dto.TableInfo;
import com.dist.datasync.config.entity.*;
import com.dist.datasync.config.repository.DataSourceConfigRepository;
import com.dist.datasync.config.repository.SyncTaskConfigRepository;
import org.apache.logging.log4j.message.ReusableMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import javax.sql.DataSource;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author lijy
 */
@Service
public class SyncTaskConfigService {

    @Autowired
    SyncTaskConfigRepository syncTaskConfigRepository;

    @Autowired
    DataSourceConfigRepository dataSourceConfigRepository;

    @Autowired
    DataSourceService dataSourceService;

    /**
     * 获取同步任务详细信息
     * @param id
     * @return
     */
    public SyncTaskConfig getConfigDetial(Integer id){
        //获取配置基本信息
        SyncTaskConfig taskConfig = syncTaskConfigRepository.findById(id).orElse(null);
        String expTableStr = taskConfig.getExceptTable()==null?"":taskConfig.getExceptTable().toUpperCase();
        String expFieldStr = taskConfig.getExceptField()==null?"":taskConfig.getExceptField().toUpperCase();
        String expSequenceStr = taskConfig.getExceptSequence()==null?"":taskConfig.getExceptSequence().toUpperCase();

        //查询数据源配置
        DataSourceConfig config = dataSourceConfigRepository.findById(taskConfig.getSourceDsId()).orElse(null);
        if(config != null){
            //建立数据源
            DataSource dataSource = dataSourceService.getDataSource(config);
            JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
            //获取表字段详细配置
            taskConfig.setTableConfigs(getTableConfigs(expTableStr, expFieldStr, jdbcTemplate));
            //获取序列详细配置
            taskConfig.setSequenceConfigs(getSequenceConfigs(expSequenceStr, jdbcTemplate));
        }
        return taskConfig;
    }

    /**
     * 根据配置配出所有需要同步的表
     * @param expTableStr
     * @param expFieldStr
     * @param jdbcTemplate
     * @return
     */
    protected List<TableConfig> getTableConfigs(String expTableStr, String expFieldStr, JdbcTemplate jdbcTemplate){
        List<TableConfig> result = new ArrayList<>();
        String sql = "select table_name from user_tables";
        List<Map<String,Object>> tableList = jdbcTemplate.queryForList(sql);
        for(Map<String,Object> table: tableList){
            String tableName = table.get("TABLE_NAME").toString();
            if(!expTableStr.contains(tableName)) {
                List<FieldConfig> fields = getFieldConfigs(tableName, expFieldStr, jdbcTemplate);
                if(fields!=null && fields.size()>0) {
                    TableConfig tableConfig = new TableConfig();
                    tableConfig.setTableName(tableName);
                    tableConfig.setSyncFields(fields);
                    result.add(tableConfig);
                }
            }
        }
        return result;
    }

    /**
     * 根据配置获取所有需要同步的字段
     * @param tableName
     * @param expFieldStr
     * @param jdbcTemplate
     * @return
     */
    protected List<FieldConfig> getFieldConfigs(String tableName, String expFieldStr, JdbcTemplate jdbcTemplate){
        List<FieldConfig> result = new ArrayList<>();
        String sql = String.format("select * from user_tab_columns where table_name = '%s'", tableName.toUpperCase());
        List<Map<String,Object>> fieldList = jdbcTemplate.queryForList(sql);
        for(Map<String,Object> field: fieldList){
            String fieldName = field.get("COLUMN_NAME").toString();
            if(!expFieldStr.contains(String.format("%s.%s",tableName,fieldName))){
                FieldConfig fieldConfig = new FieldConfig();
                fieldConfig.setName(fieldName);
                fieldConfig.setDataType(field.get("DATA_TYPE").toString());
                fieldConfig.setLength(((BigDecimal) field.get("CHAR_LENGTH")).longValue());
                Object percision = field.get("DATA_PERCICION");
                if(percision!=null) {
                    fieldConfig.setPercision(((BigDecimal) field.get("DATA_PERCICION")).longValue());
                }
                result.add(fieldConfig);
            }
        }
        return result;
    }

    /**
     * 根据配置获取所有需要同步的序列
     * @param expSequenceStr
     * @param jdbcTemplate
     * @return
     */
    protected List<SequenceConfig> getSequenceConfigs(String expSequenceStr, JdbcTemplate jdbcTemplate){
        List<SequenceConfig> result = new ArrayList<>();
        String sql = "select sequence_name from user_sequences";
        List<Map<String,Object>> sequenceList = jdbcTemplate.queryForList(sql);
        for(Map<String,Object> sequence: sequenceList){
            String name = sequence.get("SEQUENCE_NAME").toString();
            if(!expSequenceStr.contains(name)) {
                SequenceConfig sequenceConfig = new SequenceConfig();
                sequenceConfig.setSequenceName(name);
                result.add(sequenceConfig);
            }
        }
        return result;
    }

}
