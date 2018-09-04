package com.dist.datasync.config.repository;

import com.dist.datasync.config.entity.SequenceConfig;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * @author lijy
 */
public interface SequenceConfigRepository extends JpaRepository<SequenceConfig, Integer> {
    List<SequenceConfig> findByTaskId(Integer taskId);
}
