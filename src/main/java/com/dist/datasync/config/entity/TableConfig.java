package com.dist.datasync.config.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.annotations.ApiModelProperty;
import springfox.documentation.annotations.ApiIgnore;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;

/**
 * 表同步配置
 * @author lijy
 */
@Entity
public class TableConfig {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Integer id;
    @Column
    private String tableName;
    @Column(updatable = false, insertable = false)
    private Integer taskId;

    @OneToMany(cascade = CascadeType.REMOVE, fetch = FetchType.LAZY)
    @JoinColumn(name = "tableId")
    private Collection<FieldConfig> syncFields;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getTableName() {
        return tableName;
    }

    public void setTableName(String tableName) {
        this.tableName = tableName;
    }

    public Integer getTaskId() {
        return taskId;
    }

    public void setTaskId(Integer taskId) {
        this.taskId = taskId;
    }

    public Collection<FieldConfig> getSyncFields() {
        return syncFields;
    }

    public void setSyncFields(Collection<FieldConfig> syncFields) {
        this.syncFields = syncFields;
    }

    @JsonIgnore
    @ApiModelProperty(hidden = true)
    public List<String> getSyncFieldArray() {
        List<String> fieldArray = new ArrayList<>();
        for(FieldConfig fieldConfig: syncFields){
            fieldArray.add(fieldConfig.getName());
        }
        return fieldArray;
    }
}
