package com.dist.datasync.config.entity;

import javax.persistence.*;

/**
 * @author lijy
 */
@Entity
public class FieldConfig {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Integer id;
    @Column
    private String name;
    @Column
    private String dataType;
    @Column
    private Long length;
    @Column
    private Long percision;

    @Column(updatable = false, insertable = false)
    private Integer tableId;

    public FieldConfig() {
    }

    public FieldConfig(String name) {
        this.name = name;
    }

    public FieldConfig(String name, String dataType, Long length, Integer tableId) {
        this.name = name;
        this.dataType = dataType;
        this.length = length;
        this.tableId = tableId;
    }

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

    public String getDataType() {
        return dataType;
    }

    public void setDataType(String dataType) {
        this.dataType = dataType;
    }

    public Long getLength() {
        return length;
    }

    public void setLength(Long length) {
        this.length = length;
    }

    public Long getPercision() {
        return percision;
    }

    public void setPercision(Long percision) {
        this.percision = percision;
    }

    public Integer getTableId() {
        return tableId;
    }

    public void setTableId(Integer tableId) {
        this.tableId = tableId;
    }
}
