package com.dist.datasync.config.controller;

import com.dist.datasync.base.Result;
import com.dist.datasync.config.entity.FieldConfig;
import com.dist.datasync.config.repository.FieldConfigRepository;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.web.bind.annotation.*;

/**
 * @author lijy
 */
@Api(tags = "配置-字段")
@RestController
@RequestMapping("/fieldConfig")
public class FieldConfigController {

    @Autowired
    FieldConfigRepository fieldConfigRepository;

    @PostMapping("/")
    @ApiOperation(value = "保存字段配置")
    public Result<FieldConfig> save(@RequestBody FieldConfig config){
        FieldConfig fieldConfig = fieldConfigRepository.save(config);
        return Result.success(fieldConfig);
    }

    @GetMapping("/{id}")
    @ApiOperation(value = "根据ID获取字段配置")
    public Result<FieldConfig> getById(@PathVariable int id){
        FieldConfig fieldConfig = fieldConfigRepository.findById(id).orElse(null);
        return Result.success(fieldConfig);
    }

    @GetMapping("/page")
    @ApiOperation(value = "分页查询字段配置")
    public Result<Page<FieldConfig>> page(@RequestParam int page, @RequestParam int size){
        Page<FieldConfig> configPage = fieldConfigRepository.findAll(PageRequest.of(page, size));
        return Result.success(configPage);
    }

    @PostMapping("/delete/{id}")
    @ApiOperation(value = "根据ID删除字段配置")
    public Result deleteById(@PathVariable int id){
        fieldConfigRepository.deleteById(id);
        return Result.success();
    }
}
