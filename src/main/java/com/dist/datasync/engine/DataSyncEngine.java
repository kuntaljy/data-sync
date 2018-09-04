package com.dist.datasync.engine;

import com.dist.datasync.config.dto.SyncTaskModel;
import com.dist.datasync.base.BooleanWithMessage;

import java.util.Date;

/**
 * @author lijy
 */
public interface DataSyncEngine {
    BooleanWithMessage commit(SyncTaskModel syncTaskModel);

    EngineState getState();

    Float getProgress();

    Date getStartTime();

    Date getEndTime();

    String getMessage();

    Integer getExceptionCount();

    SyncTaskModel getSyncTaskConfig();
}
