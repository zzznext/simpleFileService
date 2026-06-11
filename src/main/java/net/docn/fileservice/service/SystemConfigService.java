package net.docn.fileservice.service;

import net.docn.fileservice.entity.SystemConfig;
import net.docn.fileservice.repository.SystemConfigRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 系统配置服务类
 */
@Service
public class SystemConfigService {
    
    private static final Logger log = LoggerFactory.getLogger(SystemConfigService.class);
    
    @Autowired
    private SystemConfigRepository configRepository;
    
    /**
     * 获取配置值（字符串）- 带缓存
     */
    @Cacheable(value = "systemConfig", key = "#key")
    public String getConfigValue(String key) {
        return configRepository.findByConfigKey(key)
                .map(SystemConfig::getConfigValue)
                .orElse(null);
    }
    
    /**
     * 获取配置值（整数）
     */
    public Integer getIntConfigValue(String key) {
        String value = getConfigValue(key);
        try {
            return value != null ? Integer.parseInt(value) : null;
        } catch (NumberFormatException e) {
            log.warn("配置值转换失败: {} = {}", key, value);
            return null;
        }
    }
    
    /**
     * 获取配置值（布尔）
     */
    public Boolean getBooleanConfigValue(String key) {
        String value = getConfigValue(key);
        return value != null ? Boolean.parseBoolean(value) : null;
    }
    
    /**
     * 获取配置值（长整型）
     */
    public Long getLongConfigValue(String key) {
        String value = getConfigValue(key);
        try {
            return value != null ? Long.parseLong(value) : null;
        } catch (NumberFormatException e) {
            log.warn("配置值转换失败: {} = {}", key, value);
            return null;
        }
    }
    
    /**
     * 更新配置值 - 清除缓存
     */
    @Transactional
    @CacheEvict(value = "systemConfig", key = "#key")
    public void updateConfigValue(String key, String value) {
        SystemConfig config = configRepository.findByConfigKey(key)
                .orElseThrow(() -> new RuntimeException("配置项不存在: " + key));
        
        if (!config.getIsEditable()) {
            throw new RuntimeException("配置项不可编辑: " + key);
        }
        
        String oldValue = config.getConfigValue();
        config.setConfigValue(value);
        config.setUpdatedAt(LocalDateTime.now());
        configRepository.save(config);
        
        log.info("配置已更新: {} = {} (旧值: {})", key, value, oldValue);
    }
    
    /**
     * 批量更新配置
     */
    @Transactional
    @CacheEvict(value = "systemConfig", allEntries = true)
    public void batchUpdateConfigs(Map<String, String> configs) {
        configs.forEach((key, value) -> {
            try {
                updateConfigValue(key, value);
            } catch (Exception e) {
                log.error("更新配置失败: {} = {}", key, value, e);
                throw new RuntimeException("更新配置失败: " + key + " - " + e.getMessage());
            }
        });
        log.info("批量更新配置成功，共 {} 项", configs.size());
    }
    
    /**
     * 获取某分组的所有配置
     */
    public Map<String, String> getConfigsByGroup(String group) {
        List<SystemConfig> configs = configRepository.findByConfigGroupOrderByConfigKey(group);
        return configs.stream()
                .collect(Collectors.toMap(
                    SystemConfig::getConfigKey,
                    SystemConfig::getConfigValue
                ));
    }
    
    /**
     * 获取所有配置（按分组）
     */
    public Map<String, Map<String, String>> getAllConfigsGrouped() {
        List<SystemConfig> allConfigs = configRepository.findAll();
        
        return allConfigs.stream()
                .collect(Collectors.groupingBy(
                    SystemConfig::getConfigGroup,
                    Collectors.toMap(
                        SystemConfig::getConfigKey,
                        SystemConfig::getConfigValue
                    )
                ));
    }
    
    /**
     * 检查配置项是否存在
     */
    public boolean hasConfig(String key) {
        return configRepository.findByConfigKey(key).isPresent();
    }
    
    /**
     * 获取配置项的描述信息
     */
    public String getConfigDescription(String key) {
        return configRepository.findByConfigKey(key)
                .map(SystemConfig::getDescription)
                .orElse(null);
    }
}
