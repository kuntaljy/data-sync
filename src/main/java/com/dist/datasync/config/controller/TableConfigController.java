package com.dist.datasync.config.controller;

import com.dist.datasync.base.Result;
import com.dist.datasync.config.entity.TableConfig;
import com.dist.datasync.config.repository.TableConfigRepository;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.web.bind.annotation.*;

/**
 * @author lijy
 */
@Api(tags = "配置-表")
@RestController
@RequestMapping("/tableConfig")
public class TableConfigController {

    @Autowired
    TableConfigRepository tableConfigRepository;

    @PostMapping("/")
    @ApiOperation(value = "保存表配置")
    public Result<TableConfig> save(@RequestBody TableConfig config){
        TableConfig tableConfig = tableConfigRepository.save(config);
        return Result.success(tableConfig);
    }

    @GetMapping("/{id}")
    @ApiOperation(value = "根据ID获取表配置")
    public Result<TableConfig> getById(@PathVariable int id){
        TableConfig tableConfig = tableConfigRepository.findById(id).orElse(null);
        return Result.success(tableConfig);
    }

    @GetMapping("/page")
    @ApiOperation(value = "分页查询表配置")
    public Result<Page<TableConfig>> page(@RequestParam int page, @RequestParam int size){
        Page<TableConfig> tableConfigs = tableConfigRepository.findAll(PageRequest.of(page, size));
        return Result.success(tableConfigs);
    }

    @PostMapping("/delete/{id}")
    @ApiOperation(value = "根据ID删除表配置")
    public Result deleteById(@PathVariable int id){
        tableConfigRepository.deleteById(id);
        return Result.success();
    }
}
