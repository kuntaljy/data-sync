package com.dist.datasync.engine;

import com.dist.datasync.config.dto.SyncTaskModel;
import com.dist.datasync.base.BooleanWithMessage;
import org.springframework.stereotype.Component;

import java.util.Date;

/**
 * 该同步引擎仅同一时间只允许有一个同步任务在执行，维持该任务的执行进度和状态供调用者查询，只有当状态不为“进行中”时才能执行新的同步任务。
 * @author lijy
 */
@Component
public class DefaultSyncEngine implements DataSyncEngine {

    private EngineState state = EngineState.空闲;
    private String message = "无任务执行";
    private Float progress = 0f;
    private Integer exceptionCount = 0;
    private Date startTime;
    private Date endTime;
    private SyncTaskModel syncTaskModel;


    /***
     * 提交数据库同步任务
     * @param syncTaskModel
     */
    @Override
    public BooleanWithMessage commit(SyncTaskModel syncTaskModel){
        synchronized (this) {
            //判断任务是否正在执行中
            if (this.state!= null && this.state==EngineState.执行中) {
                return BooleanWithMessage.of(false, "前序任务还没执行完");
            }
            //初始化状态数据
            this.state = EngineState.执行中;
            this.startTime = new Date();
            this.endTime = null;
            this.message = "任务进行中...";
            this.progress = 0f;
            this.exceptionCount = 0;
            this.syncTaskModel = syncTaskModel;

        }
        //调用同步处理器执行数据同步
        new Thread(){
            @Override
            public void run(){
                new SimpleSyncHandler().excute(syncTaskModel, new ProgressHandler(), new FinishHandler());
            }
        }.start();
        return BooleanWithMessage.of(true, "任务开始执行");
    }

    /**
     * 进度处理器
     */
    public final class ProgressHandler implements ProgressCallBack{
        @Override
        public void setProgress(Float progressValue) {
             progress = progressValue;
        }
    }

    /**
     * 结束状态处理器
     */
    public final class FinishHandler implements FinishCallBack {
        @Override
        public void finish(Integer expCount, Boolean success, String messageStr) {
            state = success ? EngineState.同步完成 : EngineState.同步失败;
            exceptionCount = expCount;
            endTime = new Date();
            message = messageStr;
        }
    }

    @Override
    public EngineState getState(){
        return state;
    }

    @Override
    public Float getProgress(){
        return progress;
    }

    @Override
    public Date getStartTime() {
        return startTime;
    }

    @Override
    public Date getEndTime() {
        return endTime;
    }

    @Override
    public String getMessage() {
        return message;
    }

    @Override
    public Integer getExceptionCount() {
        return exceptionCount;
    }

    @Override
    public SyncTaskModel getSyncTaskConfig() {
        return syncTaskModel;
    }
}
