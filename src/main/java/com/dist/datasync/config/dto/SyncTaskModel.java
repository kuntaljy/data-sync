package com.dist.datasync.config.dto;

import com.dist.datasync.config.entity.SyncTaskConfig;
import com.dist.datasync.config.service.DataSourceService;
import com.dist.datasync.base.SpringContext;
import org.springframework.beans.BeanUtils;

import javax.sql.DataSource;

/**
 * 同步任务配置模型
 * @author lijy
 */
public class SyncTaskModel extends SyncTaskConfig {

    private DataSource source;
    private DataSource target;

    public void setSource(DataSource source) {
        this.source = source;
    }

    public void setTarget(DataSource target) {
        this.target = target;
    }

    /**
     * 获取源库数据源
     * @return
     */
    public DataSource getSource() {
        if(source==null){
            synchronized (this){
                if(source==null){
                    DataSourceService service = SpringContext.getBean(DataSourceService.class);
                    source = service.getDataSource(this.getSourceDsId());
                }
            }
        }
        return source;
    }

    /**
     * 获取目标库数据源
     * @return
     */
    public DataSource getTarget() {
        if(target==null){
            synchronized (this){
                if(target==null){
                    DataSourceService service = SpringContext.getBean(DataSourceService.class);
                    target = service.getDataSource(this.getTargetDsId());
                }
            }
        }
        return target;
    }

    /**
     * 通过SyncTaskConfig创建一个SyncTaskModel
     * @param config
     * @return
     */
    public static SyncTaskModel of(SyncTaskConfig config){
        SyncTaskModel taskModel = new SyncTaskModel();
        BeanUtils.copyProperties(config, taskModel);
        return taskModel;
    }
}
