package com.dist.datasync.config.controller;

import com.dist.datasync.base.Result;
import com.dist.datasync.config.entity.DataSourceConfig;
import com.dist.datasync.config.repository.DataSourceConfigRepository;
import com.dist.datasync.config.service.DataSourceService;
import com.dist.datasync.base.BooleanWithMessage;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.web.bind.annotation.*;

/**
 * @author lijy
 */
@Api(tags = "配置-数据源")
@RestController
@RequestMapping("/dataSource")
public class DataSourceConfigController {

    @Autowired
    DataSourceConfigRepository dataSourceConfigRepository;

    @Autowired
    DataSourceService dataSourceService;

    @PostMapping("/")
    @ApiOperation(value = "保存数据源配置")
    public Result<DataSourceConfig> save(@RequestBody DataSourceConfig config){
        config = dataSourceConfigRepository.save(config);
        return Result.success(config);
    }

    @PostMapping("/test")
    @ApiOperation(value = "测试数据源配置")
    public Result test(@RequestBody DataSourceConfig config){
        BooleanWithMessage bwm = dataSourceService.testDataSource(config);
        return Result.of(bwm);
    }

    @GetMapping("/{id}")
    @ApiOperation(value = "根据ID获取数据源配置")
    public Result<DataSourceConfig> getById(@PathVariable int id){
        DataSourceConfig config = dataSourceConfigRepository.findById(id).orElse(null);
        return Result.success(config);
    }

    @GetMapping("/page")
    @ApiOperation(value = "分页查询数据源配置")
    public Result<Page<DataSourceConfig>> page(@RequestParam int page, @RequestParam int size){
        Page<DataSourceConfig> configPage = dataSourceConfigRepository.findAll(PageRequest.of(page, size));
        return Result.success(configPage);
    }

    @PostMapping("/delete/{id}")
    @ApiOperation(value = "根据ID删除数据源配置")
    public Result deleteById(@PathVariable int id){
        dataSourceConfigRepository.deleteById(id);
        return Result.success();
    }
}
