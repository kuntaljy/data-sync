package com.dist.datasync.config.controller;

import com.dist.datasync.base.Result;
import com.dist.datasync.config.entity.SyncTaskConfig;
import com.dist.datasync.config.repository.SyncTaskConfigRepository;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.web.bind.annotation.*;

/**
 * @author lijy
 */
@Api(tags = "配置-任务")
@RestController
@RequestMapping("/syncTaskConfig")
public class SyncTaskConfigController {

    @Autowired
    SyncTaskConfigRepository syncTaskConfigRepository;

    @PostMapping("/")
    @ApiOperation(value = "保存同步任务配置")
    public Result<SyncTaskConfig> save(@RequestBody SyncTaskConfig config){
        SyncTaskConfig taskConfig = syncTaskConfigRepository.save(config);
        return Result.success(taskConfig);
    }

    @GetMapping("/{id}")
    @ApiOperation(value = "根据ID获取同步任务配置")
    public Result<SyncTaskConfig> getById(@PathVariable int id){
        SyncTaskConfig taskConfig = syncTaskConfigRepository.findById(id).orElse(null);
        return Result.success(taskConfig);
    }

    @GetMapping("/page")
    @ApiOperation(value = "分页查询同步任务配置")
    public Result<Page<SyncTaskConfig>> page(@RequestParam int page, @RequestParam int size){
        Page<SyncTaskConfig> taskConfigs = syncTaskConfigRepository.findAll(PageRequest.of(page, size));
        return  Result.success(taskConfigs);
    }

    @PostMapping("/delete/{id}")
    @ApiOperation(value = "根据ID删除同步任务配置")
    public Result deleteById(@PathVariable int id){
        syncTaskConfigRepository.deleteById(id);
        return Result.success();
    }
}
