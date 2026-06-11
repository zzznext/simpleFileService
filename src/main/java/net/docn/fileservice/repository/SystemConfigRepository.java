package net.docn.fileservice.repository;

import net.docn.fileservice.entity.SystemConfig;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * 系统配置 Repository
 */
@Repository
public interface SystemConfigRepository extends JpaRepository<SystemConfig, Long> {
    
    /**
     * 根据配置键名查找
     */
    Optional<SystemConfig> findByConfigKey(String configKey);
    
    /**
     * 根据配置分组查找（按 config_key 排序）
     */
    List<SystemConfig> findByConfigGroupOrderByConfigKey(String configGroup);
    
    /**
     * 批量更新配置值
     */
    @Modifying
    @Query("UPDATE SystemConfig c SET c.configValue = :value, c.updatedAt = :now WHERE c.configKey = :key")
    void updateConfigValue(@Param("key") String key, 
                          @Param("value") String value, 
                          @Param("now") LocalDateTime now);
}
