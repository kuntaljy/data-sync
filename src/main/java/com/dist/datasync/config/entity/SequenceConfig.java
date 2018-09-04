package com.dist.datasync.config.entity;

import javax.persistence.*;

/**
 * 序列同步配置
 * @author lijy
 */
@Entity
public class SequenceConfig {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Integer id;
    @Column
    private String sequenceName;
    @Column(updatable = false, insertable = false)
    private Integer taskId;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getSequenceName() {
        return sequenceName;
    }

    public void setSequenceName(String sequenceName) {
        this.sequenceName = sequenceName;
    }

    public Integer getTaskId() {
        return taskId;
    }

    public void setTaskId(Integer taskId) {
        this.taskId = taskId;
    }
}
