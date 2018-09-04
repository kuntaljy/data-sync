package com.dist.datasync.config.controller;

import com.dist.datasync.base.Result;
import com.dist.datasync.config.entity.SequenceConfig;
import com.dist.datasync.config.repository.SequenceConfigRepository;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.web.bind.annotation.*;

/**
 * @author lijy
 */
@Api(tags = "配置-序列")
@RestController
@RequestMapping("/sequenceConfig")
public class SequenceConfigController {

    @Autowired
    SequenceConfigRepository sequenceConfigRepository;

    @PostMapping("/")
    @ApiOperation(value = "保存序列配置")
    public Result<SequenceConfig> save(@RequestBody SequenceConfig config){
        SequenceConfig sequenceConfig = sequenceConfigRepository.save(config);
        return Result.success(sequenceConfig);
    }

    @GetMapping("/{id}")
    @ApiOperation(value = "根据ID获取序列配置")
    public Result<SequenceConfig> getById(@PathVariable int id){
        SequenceConfig sequenceConfig = sequenceConfigRepository.findById(id).orElse(null);
        return Result.success(sequenceConfig);
    }

    @GetMapping("/page")
    @ApiOperation(value = "分页查询序列配置")
    public Result<Page<SequenceConfig>> page(@RequestParam int page, @RequestParam int size){
        Page<SequenceConfig> configs = sequenceConfigRepository.findAll(PageRequest.of(page, size));
        return Result.success(configs);
    }

    @PostMapping("/delete/{id}")
    @ApiOperation(value = "根据ID删除序列配置")
    public Result deleteById(@PathVariable int id){
        sequenceConfigRepository.deleteById(id);
        return Result.success();
    }
}
