package com.dist.datasync.config.repository;

import com.dist.datasync.config.entity.SyncTaskConfig;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * @author lijy
 */
public interface SyncTaskConfigRepository extends JpaRepository<SyncTaskConfig, Integer>{
}
