package com.dist.datasync.config.service;

import com.dist.datasync.config.entity.DataSourceConfig;
import com.dist.datasync.config.repository.DataSourceConfigRepository;
import com.dist.datasync.base.BooleanWithMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import javax.sql.DataSource;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 根据数据源配置生成数据源
 * 对已经生成的数据源进行缓存，避免重复创建
 * @author lijy
 */
@Service
public class DataSourceService {

    @Autowired
    DataSourceConfigRepository dataSourceConfigRepository;

    /**
     * 数据源缓存，使用ConcurrentHashMap对象，防止并发异常
     */
    private Map<String,DataSource> dataSourceCache = new ConcurrentHashMap<>();

    /**
     * 测试数据源是否能连接成功
     * @param config
     * @return
     */
    public BooleanWithMessage testDataSource(DataSourceConfig config){
        DataSource dataSource = getDataSource(config);
        try {
            JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
            Map<String, Object> result = jdbcTemplate.queryForMap("select 'test' TEST from dual");
            if(result != null && result.size() > 0 && result.get("TEST").equals("test")) {
                return BooleanWithMessage.of(true, "链接成功");
            }else {
                return BooleanWithMessage.of(false, "链接失败");
            }
        }catch (DataAccessException e){
            return BooleanWithMessage.of(false, e.getMessage());
        }
    }

    /**
     * 根据数据源配置获取数据源对象
     * 并将已经创建的数据源对象进行缓存，下一次先从缓存获取
     * @param dataSourceConfigId
     * @return
     */
    public DataSource getDataSource(Integer dataSourceConfigId){
        DataSourceConfig dataSourceConfig = dataSourceConfigRepository.findById(dataSourceConfigId).orElse(null);
        return getDataSource(dataSourceConfig);
    }

    /**
     * 根据数据源配置获取数据源对象
     * 并将已经创建的数据源对象进行缓存，下一次先从缓存获取
     * @param config
     * @return
     */
    public DataSource getDataSource(DataSourceConfig config){
        String cacheKey = getCacheKey(config);
        DataSource dataSource = dataSourceCache.get(cacheKey);
        if(dataSource==null) {
            DataSourceBuilder dataSourceBuilder = DataSourceBuilder.create();
            dataSourceBuilder.driverClassName(config.getDriverClass());
            dataSourceBuilder.url(config.getUrl());
            dataSourceBuilder.username(config.getUserName());
            dataSourceBuilder.password(config.getPassWord());
            dataSource = dataSourceBuilder.build();
            dataSourceCache.put(cacheKey, dataSource);
        }
        return dataSource;
    }

    /**
     * 生成缓存key
     * 注意不能用config对象id作为缓存的key，同一个id的config内容可能不同，
     * 不同id的config的实际内容却可能一样，因此以config的实际内容拼接结果作为key
     * @param config
     * @return
     */
    protected String getCacheKey(DataSourceConfig config){
        StringBuilder builder = new StringBuilder();
        builder.append(config.getDriverClass()).append("|").
                append(config.getUrl()).append("|").
                append(config.getUserName()).append("|").
                append(config.getPassWord());
        return builder.toString();
    }
}
