package com.dist.datasync.engine;

import com.dist.datasync.config.dto.SyncTaskModel;

/**
 * 同步处理器接口
 * 将指定表从source数据源同步到target数据源，如果存在
 * @author lijy
 */
public interface SyncHandler {
    void excute(SyncTaskModel syncTaskModel, ProgressCallBack progressCallBack, FinishCallBack finishCallBack);
}
