package com.dist.datasync.config.service;

import com.dist.datasync.config.dto.EngineInfo;
import com.dist.datasync.config.entity.SyncTaskConfig;
import com.dist.datasync.config.dto.SyncTaskModel;
import com.dist.datasync.config.repository.SyncTaskConfigRepository;
import com.dist.datasync.engine.DataSyncEngine;
import com.dist.datasync.engine.EngineState;
import com.dist.datasync.base.BooleanWithMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 调用SyncEngine触发同步任务的执行
 * @author lijy
 */
@Service
public class DataSyncService {

    @Autowired
    DataSyncEngine dataSyncEngine;

    @Autowired
    SyncTaskConfigService syncTaskConfigService;

    /**
     * 根据同步任务配置触发数据同步
     * @param id
     * @return
     */
    public BooleanWithMessage executeTask(int id){
        EngineState state = dataSyncEngine.getState();
        if(state==EngineState.执行中) {
            return BooleanWithMessage.of(false, "前序任务还没执行完，不能开启信息任务");
        }else {
            SyncTaskConfig config = syncTaskConfigService.getConfigDetial(id);
            if(config==null){
                return BooleanWithMessage.of(false, "获取任务信息失败");
            }else {
                SyncTaskModel taskInfo = SyncTaskModel.of(config);
                return dataSyncEngine.commit(taskInfo);
            }
        }
    }

    /**
     * 获取执行引擎当前状态
     * @return
     */
    public String getEnginState(){
        EngineState state = dataSyncEngine.getState();
        return state.toString();
    }


    /**
     * 获取执行引擎当前状态描述
     * @return
     */
    public EngineInfo getEngineInfo(){
        EngineInfo info = new EngineInfo();
        if(dataSyncEngine.getSyncTaskConfig()!=null) {
            info.setTaskName(dataSyncEngine.getSyncTaskConfig().getName());
        }
        info.setState(dataSyncEngine.getState().toString());
        info.setProgress(dataSyncEngine.getProgress());
        info.setExceptionCount(dataSyncEngine.getExceptionCount());
        info.setMessage(dataSyncEngine.getMessage());
        info.setStartTime(dataSyncEngine.getStartTime());
        info.setEndTime(dataSyncEngine.getEndTime());
        return info;
    }
}
