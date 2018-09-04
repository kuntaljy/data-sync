package com.dist.datasync.config.repository;

import com.dist.datasync.config.entity.DataSourceConfig;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * @author lijy
 */
public interface DataSourceConfigRepository extends JpaRepository<DataSourceConfig,Integer> {
}
