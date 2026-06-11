package net.docn.fileservice.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * 系统配置实体类
 */
@Entity
@Table(name = "system_config")
public class SystemConfig {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "config_key", nullable = false, unique = true, length = 100)
    private String configKey;
    
    @Column(name = "config_value", columnDefinition = "TEXT")
    private String configValue;
    
    @Column(name = "config_type", nullable = false, length = 20)
    private String configType; // STRING, INTEGER, BOOLEAN, JSON
    
    @Column(name = "config_group", nullable = false, length = 50)
    private String configGroup; // STORAGE, SECURITY, FILE, SYSTEM, EMAIL
    
    @Column(name = "description", length = 500)
    private String description;
    
    @Column(name = "is_editable", nullable = false)
    private Boolean isEditable = true;
    
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
    
    // Constructors
    public SystemConfig() {
    }
    
    public SystemConfig(String configKey, String configValue, String configType, 
                       String configGroup, String description, Boolean isEditable) {
        this.configKey = configKey;
        this.configValue = configValue;
        this.configType = configType;
        this.configGroup = configGroup;
        this.description = description;
        this.isEditable = isEditable;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }
    
    // Getters and Setters
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public String getConfigKey() {
        return configKey;
    }
    
    public void setConfigKey(String configKey) {
        this.configKey = configKey;
    }
    
    public String getConfigValue() {
        return configValue;
    }
    
    public void setConfigValue(String configValue) {
        this.configValue = configValue;
    }
    
    public String getConfigType() {
        return configType;
    }
    
    public void setConfigType(String configType) {
        this.configType = configType;
    }
    
    public String getConfigGroup() {
        return configGroup;
    }
    
    public void setConfigGroup(String configGroup) {
        this.configGroup = configGroup;
    }
    
    public String getDescription() {
        return description;
    }
    
    public void setDescription(String description) {
        this.description = description;
    }
    
    public Boolean getIsEditable() {
        return isEditable;
    }
    
    public void setIsEditable(Boolean isEditable) {
        this.isEditable = isEditable;
    }
    
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
    
    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
    
    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
    
    @Override
    public String toString() {
        return "SystemConfig{" +
                "id=" + id +
                ", configKey='" + configKey + '\'' +
                ", configValue='" + configValue + '\'' +
                ", configType='" + configType + '\'' +
                ", configGroup='" + configGroup + '\'' +
                ", description='" + description + '\'' +
                ", isEditable=" + isEditable +
                ", createdAt=" + createdAt +
                ", updatedAt=" + updatedAt +
                '}';
    }
}
