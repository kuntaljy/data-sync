package com.dist.datasync.config.repository;

import com.dist.datasync.config.entity.TableConfig;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * @author lijy
 */
public interface TableConfigRepository extends JpaRepository<TableConfig, Integer> {
    List<TableConfig> findByTaskId(Integer taskId);
}
