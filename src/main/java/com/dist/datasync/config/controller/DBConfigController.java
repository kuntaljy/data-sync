package com.dist.datasync.config.controller;

import com.dist.datasync.base.Result;
import com.dist.datasync.config.dto.FieldInfo;
import com.dist.datasync.config.dto.SequenceInfo;
import com.dist.datasync.config.dto.TableInfo;
import com.dist.datasync.config.service.DBConfigService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.naming.Name;
import java.util.List;

/**
 * @author lijy
 */
@Api(tags = "配置")
@RestController
@RequestMapping("/dbConfig")
public class DBConfigController {

    @Autowired
    DBConfigService dbConfigService;

    @GetMapping("/tables")
    @ApiOperation(value = "获取所有表配置信息")
    public Result<List<TableInfo>> getAllTalbes(@RequestParam Integer dataSourceConfigId, @RequestParam Integer syncTaskConfigId){
        List<TableInfo> tableInfos = dbConfigService.getAllTables(dataSourceConfigId, syncTaskConfigId);
        return Result.success(tableInfos);
    }

    @GetMapping("/fields")
    @ApiOperation(value = "获取表对应的字段配置信息")
    public Result<List<FieldInfo>> getFieldsOfTable(@RequestParam Integer dataSourceConfigId, @RequestParam String tableName, @RequestParam Integer tableId){
        List<FieldInfo> fieldInfos = dbConfigService.getFieldsOfTable(dataSourceConfigId, tableName, tableId);
        return Result.success(fieldInfos);
    }

    @GetMapping("/sequences")
    @ApiOperation(value = "获所有序列配置信息")
    public Result<List<SequenceInfo>> getAllSequences(@RequestParam Integer dataSourceConfigId, @RequestParam Integer syncTaskConfigId){
        List<SequenceInfo> sequenceInfos = dbConfigService.getAllSequences(dataSourceConfigId, syncTaskConfigId);
        return Result.success(sequenceInfos);
    }
}
