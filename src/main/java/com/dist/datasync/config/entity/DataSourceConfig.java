package com.dist.datasync.config.entity;

import org.springframework.boot.jdbc.DataSourceBuilder;

import javax.persistence.*;
import javax.sql.DataSource;
import java.awt.image.renderable.ContextualRenderedImageFactory;

/**
 * @author lijy
 */
@Entity
public class DataSourceConfig {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Integer id;
    @Column
    private String name;
    @Column
    private String driverClass;
    @Column
    private String url;
    @Column
    private String userName;
    @Column
    private String passWord;

    public DataSourceConfig() {
    }

    public DataSourceConfig(String driverClass, String url, String userName, String passWord) {
        this.driverClass = driverClass;
        this.url = url;
        this.userName = userName;
        this.passWord = passWord;
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

    public String getDriverClass() {
        return driverClass;
    }

    public void setDriverClass(String driverClass) {
        this.driverClass = driverClass;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getPassWord() {
        return passWord;
    }

    public void setPassWord(String passWord) {
        this.passWord = passWord;
    }
}
