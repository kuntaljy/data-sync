package com.dist.datasync.config.controller;

import com.dist.datasync.base.Result;
import com.dist.datasync.config.dto.EngineInfo;
import com.dist.datasync.config.service.DataSyncService;
import com.dist.datasync.base.BooleanWithMessage;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * @author lijy
 */
@Api(tags = "同步")
@RestController
@RequestMapping("/dataSync")
public class DataSyncController {

    @Autowired
    DataSyncService syncService;

    @GetMapping("/state")
    @ApiOperation(value = "获取同步引擎当前信息")
    public Result<EngineInfo> getEngineInfo(){
        EngineInfo info = syncService.getEngineInfo();
        return Result.success(info);
    }

    @PostMapping("/execute/{syncTaskConfigId}")
    @ApiOperation(value = "根据同步任务配置的ID执行同步任务")
    public Result excuteTask(@PathVariable Integer syncTaskConfigId){
        BooleanWithMessage bwm = syncService.executeTask(syncTaskConfigId);
        return Result.of(bwm);
    }
}
