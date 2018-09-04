package com.dist.datasync.config.repository;

import com.dist.datasync.config.entity.FieldConfig;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * @author lijy
 */
public interface FieldConfigRepository extends JpaRepository<FieldConfig, Integer> {
    List<FieldConfig> findByTableId(Integer tableId);
}
