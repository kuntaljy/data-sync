package com.dist.datasync.config.entity;

import javax.persistence.*;
import java.util.Collection;
import java.util.Date;

/**
 * 同步任务配置
 * @author lijy
 */
@Entity
public class SyncTaskConfig {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Integer id;
    @Column
    private String name;
    @Column
    private Date createTime;
    @Column
    private Integer sourceDsId;
    @Column
    private Integer targetDsId;
    @Column(length = 4000)
    private String exceptTable;
    @Column(length = 4000)
    private String exceptField;
    @Column(length = 4000)
    private String exceptSequence;

    @OneToMany(cascade = CascadeType.REMOVE, fetch = FetchType.LAZY)
    @JoinColumn(name = "taskId")
    private Collection<TableConfig> tableConfigs;

    @OneToMany(cascade = CascadeType.REMOVE, fetch = FetchType.LAZY)
    @JoinColumn(name = "taskId")
    private Collection<SequenceConfig> sequenceConfigs;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    public Integer getSourceDsId() {
        return sourceDsId;
    }

    public void setSourceDsId(Integer sourceDsId) {
        this.sourceDsId = sourceDsId;
    }

    public Integer getTargetDsId() {
        return targetDsId;
    }

    public void setTargetDsId(Integer targetDsId) {
        this.targetDsId = targetDsId;
    }

    public String getExceptTable() {
        return exceptTable;
    }

    public void setExceptTable(String exceptTable) {
        this.exceptTable = exceptTable;
    }

    public String getExceptField() {
        return exceptField;
    }

    public void setExceptField(String exceptField) {
        this.exceptField = exceptField;
    }

    public String getExceptSequence() {
        return exceptSequence;
    }

    public void setExceptSequence(String exceptSequence) {
        this.exceptSequence = exceptSequence;
    }

    public Collection<TableConfig> getTableConfigs() {
        return tableConfigs;
    }

    public void setTableConfigs(Collection<TableConfig> tableConfigs) {
        this.tableConfigs = tableConfigs;
    }

    public Collection<SequenceConfig> getSequenceConfigs() {
        return sequenceConfigs;
    }

    public void setSequenceConfigs(Collection<SequenceConfig> sequenceConfigs) {
        this.sequenceConfigs = sequenceConfigs;
    }
}
