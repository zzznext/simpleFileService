# 系统设置功能设计文档

## 📋 项目概述

### 背景
当前 FileService 系统已经实现了用户管理、文件管理等核心功能，但缺少统一的系统配置管理界面。管理员需要通过修改配置文件或数据库来调整系统参数，操作不便且容易出错。

### 目标
开发一个可视化的系统设置管理模块，让管理员可以通过 Web 界面轻松配置和管理系统的各项参数，包括：
- 存储容量限制
- 文件类型白名单
- 文件大小限制
- 系统安全策略
- 邮件通知配置
- 系统维护模式

---

## 🎯 功能需求

### 1. 存储配置

#### 1.1 全局存储设置
- **最大存储容量**：设置系统总存储空间（单位：GB）
- **单用户存储配额**：设置每个用户的默认存储配额（单位：GB）
- **存储警告阈值**：当存储使用率达到该百分比时发出警告（默认 80%）
- **存储清理策略**：自动删除过期文件的策略选项

#### 1.2 上传限制
- **单个文件最大大小**：限制单个文件的最大上传大小（单位：MB）
- **每日上传配额**：每个用户每天最多可上传的文件数量
- **并发上传限制**：同时允许的最大上传任务数

---

### 2. 文件类型配置

#### 2.1 允许的文件类型
- **图片文件**：jpg, jpeg, png, gif, webp, svg, bmp
- **文档文件**：pdf, doc, docx, xls, xlsx, ppt, pptx, txt
- **压缩文件**：zip, rar, 7z, tar, gz
- **音频文件**：mp3, wav, flac, aac, ogg
- **视频文件**：mp4, avi, mov, mkv, webm
- **代码文件**：js, css, html, py, java, cpp, go

#### 2.2 文件类型验证
- **启用扩展名检查**：基于文件扩展名的白名单验证
- **启用 MIME 类型检查**：基于文件内容的 MIME 类型验证
- **严格模式**：同时检查扩展名和 MIME 类型（推荐）

---

### 3. 安全策略配置

#### 3.1 密码策略
- **最小密码长度**：6-128 字符（默认 8）
- **要求大写字母**：是否必须包含大写字母
- **要求小写字母**：是否必须包含小写字母
- **要求数字**：是否必须包含数字
- **要求特殊字符**：是否必须包含特殊字符（!@#$%^&*等）
- **密码有效期**：密码强制更换周期（天，0=永不过期）

#### 3.2 登录安全
- **最大登录失败次数**：超过此次数后锁定账户（0=不限制）
- **账户锁定时长**：账户锁定后的解锁时间（分钟）
- **会话超时时间**：用户无操作后的自动登出时间（分钟）
- **强制单点登录**：同一账号是否只允许一个活跃会话

#### 3.3 验证码设置
- **启用登录验证码**：是否需要输入验证码
- **验证码复杂度**：简单（4位数字）/ 中等（5位字母数字）/ 复杂（6位混合）
- **验证码有效期**：验证码的有效时间（秒）

---

### 4. 系统维护配置

#### 4.1 维护模式
- **启用维护模式**：开启后普通用户无法访问系统
- **维护提示信息**：向用户显示的维护消息
- **维护白名单 IP**：即使开启维护模式也能访问的 IP 地址列表

#### 4.2 日志配置
- **日志级别**：DEBUG / INFO / WARN / ERROR
- **日志保留天数**：系统日志的保留时间（天）
- **启用操作审计**：记录用户的关键操作日志

#### 4.3 备份策略
- **自动备份**：是否启用数据库自动备份
- **备份频率**：每天 / 每周 / 每月
- **备份保留数量**：保留最近的备份文件数量
- **备份存储路径**：备份文件的存储位置

---

### 5. 邮件通知配置（可选）

#### 5.1 SMTP 服务器设置
- **SMTP 服务器地址**：如 smtp.example.com
- **SMTP 端口**：如 587（TLS）或 465（SSL）
- **启用 SSL/TLS**：是否使用加密连接
- **发件人邮箱**：用于发送邮件的邮箱地址
- **用户名**：SMTP 认证用户名
- **密码**：SMTP 认证密码

#### 5.2 通知触发条件
- **新用户注册通知**：有新用户注册时发送邮件
- **存储超限通知**：用户存储使用超限时发送警告
- **系统异常通知**：系统出现错误时通知管理员
- **定期报告**：每周/每月发送系统运行报告

---

## 🏗️ 技术架构

### 1. 数据库设计

#### 1.1 系统配置表

```sql
CREATE TABLE system_config (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    config_key VARCHAR(100) NOT NULL UNIQUE COMMENT '配置键名',
    config_value TEXT COMMENT '配置值',
    config_type VARCHAR(20) NOT NULL DEFAULT 'STRING' COMMENT '配置类型: STRING, INTEGER, BOOLEAN, JSON',
    config_group VARCHAR(50) NOT NULL COMMENT '配置分组: STORAGE, SECURITY, FILE, SYSTEM, EMAIL',
    description VARCHAR(500) COMMENT '配置描述',
    is_editable BOOLEAN DEFAULT TRUE COMMENT '是否可编辑',
    created_at DATETIME NOT NULL,
    updated_at DATETIME NOT NULL,
    INDEX idx_config_group (config_group),
    INDEX idx_config_key (config_key)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='系统配置表';
```

#### 1.2 初始化数据

```sql
-- 存储配置
INSERT INTO system_config (config_key, config_value, config_type, config_group, description) VALUES
('storage.max_total_size_gb', '100', 'INTEGER', 'STORAGE', '系统总存储容量（GB）'),
('storage.user_quota_gb', '10', 'INTEGER', 'STORAGE', '单用户存储配额（GB）'),
('storage.warning_threshold_percent', '80', 'INTEGER', 'STORAGE', '存储警告阈值（%）'),
('upload.max_file_size_mb', '10', 'INTEGER', 'STORAGE', '单个文件最大大小（MB）'),
('upload.daily_quota', '100', 'INTEGER', 'STORAGE', '每日上传配额（个）');

-- 文件类型配置
INSERT INTO system_config (config_key, config_value, config_type, config_group, description) VALUES
('file.allowed_extensions', 'jpg,jpeg,png,gif,pdf,doc,docx,zip,rar,txt,csv', 'STRING', 'FILE', '允许的文件扩展名（逗号分隔）'),
('file.enable_mime_check', 'true', 'BOOLEAN', 'FILE', '启用 MIME 类型检查'),
('file.validation_mode', 'strict', 'STRING', 'FILE', '文件验证模式: simple, strict');

-- 安全策略
INSERT INTO system_config (config_key, config_value, config_type, config_group, description) VALUES
('security.min_password_length', '8', 'INTEGER', 'SECURITY', '最小密码长度'),
('security.require_uppercase', 'false', 'BOOLEAN', 'SECURITY', '要求大写字母'),
('security.require_lowercase', 'true', 'BOOLEAN', 'SECURITY', '要求小写字母'),
('security.require_digit', 'true', 'BOOLEAN', 'SECURITY', '要求数字'),
('security.max_login_attempts', '5', 'INTEGER', 'SECURITY', '最大登录失败次数'),
('security.account_lockout_duration', '30', 'INTEGER', 'SECURITY', '账户锁定时长（分钟）'),
('security.session_timeout', '30', 'INTEGER', 'SECURITY', '会话超时时间（分钟）');

-- 系统配置
INSERT INTO system_config (config_key, config_value, config_type, config_group, description) VALUES
('system.maintenance_mode', 'false', 'BOOLEAN', 'SYSTEM', '维护模式'),
('system.log_level', 'INFO', 'STRING', 'SYSTEM', '日志级别'),
('system.log_retention_days', '30', 'INTEGER', 'SYSTEM', '日志保留天数');
```

---

### 2. 后端设计

#### 2.1 实体类

```java
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
    
    @Column(name = "is_editable")
    private Boolean isEditable = true;
    
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    // getters and setters
}
```

#### 2.2 Repository

```java
@Repository
public interface SystemConfigRepository extends JpaRepository<SystemConfig, Long> {
    
    // 根据配置键名查找
    Optional<SystemConfig> findByConfigKey(String configKey);
    
    // 根据配置分组查找
    List<SystemConfig> findByConfigGroupOrderByConfigKey(String configGroup);
    
    // 批量更新配置值
    @Modifying
    @Query("UPDATE SystemConfig c SET c.configValue = :value, c.updatedAt = :now WHERE c.configKey = :key")
    void updateConfigValue(@Param("key") String key, 
                          @Param("value") String value, 
                          @Param("now") LocalDateTime now);
}
```

#### 2.3 Service

```java
@Service
public class SystemConfigService {
    
    @Autowired
    private SystemConfigRepository configRepository;
    
    /**
     * 获取配置值（字符串）
     */
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
        return value != null ? Integer.parseInt(value) : null;
    }
    
    /**
     * 获取配置值（布尔）
     */
    public Boolean getBooleanConfigValue(String key) {
        String value = getConfigValue(key);
        return value != null ? Boolean.parseBoolean(value) : null;
    }
    
    /**
     * 更新配置值
     */
    @Transactional
    public void updateConfigValue(String key, String value) {
        SystemConfig config = configRepository.findByConfigKey(key)
                .orElseThrow(() -> new RuntimeException("配置项不存在: " + key));
        
        if (!config.getIsEditable()) {
            throw new RuntimeException("配置项不可编辑: " + key);
        }
        
        config.setConfigValue(value);
        config.setUpdatedAt(LocalDateTime.now());
        configRepository.save(config);
    }
    
    /**
     * 批量更新配置
     */
    @Transactional
    public void batchUpdateConfigs(Map<String, String> configs) {
        configs.forEach(this::updateConfigValue);
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
}
```

#### 2.4 Controller

```java
@Controller
@RequestMapping("/admin/settings")
public class SystemSettingsController {
    
    @Autowired
    private SystemConfigService configService;
    
    /**
     * 系统设置主页
     */
    @GetMapping
    public String settingsPage(Model model) {
        // 获取所有分组的配置
        model.addAttribute("storageConfigs", configService.getConfigsByGroup("STORAGE"));
        model.addAttribute("fileConfigs", configService.getConfigsByGroup("FILE"));
        model.addAttribute("securityConfigs", configService.getConfigsByGroup("SECURITY"));
        model.addAttribute("systemConfigs", configService.getConfigsByGroup("SYSTEM"));
        
        return "admin/settings";
    }
    
    /**
     * 保存存储配置
     */
    @PostMapping("/storage")
    public String saveStorageConfig(@RequestParam Map<String, String> configs,
                                   RedirectAttributes redirectAttributes) {
        try {
            configService.batchUpdateConfigs(configs);
            redirectAttributes.addFlashAttribute("message", "存储配置保存成功");
        } catch (Exception e) {
            log.error("保存存储配置失败", e);
            redirectAttributes.addFlashAttribute("error", "保存失败: " + e.getMessage());
        }
        return "redirect:/admin/settings";
    }
    
    /**
     * 保存文件类型配置
     */
    @PostMapping("/file")
    public String saveFileConfig(@RequestParam Map<String, String> configs,
                                RedirectAttributes redirectAttributes) {
        // 类似实现
    }
    
    /**
     * 保存安全策略配置
     */
    @PostMapping("/security")
    public String saveSecurityConfig(@RequestParam Map<String, String> configs,
                                    RedirectAttributes redirectAttributes) {
        // 类似实现
    }
    
    /**
     * 保存系统配置
     */
    @PostMapping("/system")
    public String saveSystemConfig(@RequestParam Map<String, String> configs,
                                  RedirectAttributes redirectAttributes) {
        // 类似实现
    }
}
```

---

### 3. 前端设计

#### 3.1 页面布局

```
┌─────────────────────────────────────┐
│        系统设置                      │
├─────────────────────────────────────┤
│  [存储配置] [文件类型] [安全策略]   │
│  [系统维护] [邮件通知]              │
├─────────────────────────────────────┤
│                                     │
│  选项卡内容区域                      │
│  - 表单字段                         │
│  - 输入框/下拉框/开关               │
│  - 保存按钮                         │
│                                     │
└─────────────────────────────────────┘
```

#### 3.2 选项卡设计

**选项卡 1：存储配置**
- 系统总存储容量（数字输入框，单位 GB）
- 单用户存储配额（数字输入框，单位 GB）
- 存储警告阈值（滑块，0-100%）
- 单个文件最大大小（数字输入框，单位 MB）
- 每日上传配额（数字输入框）

**选项卡 2：文件类型**
- 允许的文件类型（多选复选框组）
- 自定义扩展名（文本域，逗号分隔）
- 文件验证模式（单选：简单/严格）
- 启用 MIME 类型检查（开关）

**选项卡 3：安全策略**
- 最小密码长度（数字输入框）
- 密码复杂度要求（复选框组）
- 密码有效期（数字输入框，天）
- 最大登录失败次数（数字输入框）
- 账户锁定时长（数字输入框，分钟）
- 会话超时时间（数字输入框，分钟）
- 启用验证码（开关）

**选项卡 4：系统维护**
- 启用维护模式（开关）
- 维护提示信息（文本域）
- 日志级别（下拉框）
- 日志保留天数（数字输入框）
- 启用操作审计（开关）

**选项卡 5：邮件通知**
- SMTP 服务器地址（文本输入框）
- SMTP 端口（数字输入框）
- 启用 SSL/TLS（开关）
- 发件人邮箱（文本输入框）
- 用户名/密码（密码输入框）
- 测试邮件发送（按钮）

---

## 🔐 权限控制

### 1. 访问权限
- **仅管理员可访问**：`/admin/settings/**` 路径需要 `ROLE_ADMIN` 权限
- **CSRF 保护**：所有 POST 请求必须携带 CSRF token

### 2. 敏感配置保护
- 某些关键配置（如数据库连接）设置为 `is_editable = false`
- 修改安全策略时需要二次确认
- 记录所有配置变更的审计日志

---

## 📊 配置缓存策略

### 1. 内存缓存
使用 Spring Cache 缓存常用配置，避免频繁查询数据库：

```java
@Service
public class SystemConfigService {
    
    @Cacheable(value = "systemConfig", key = "#key")
    public String getConfigValue(String key) {
        // ...
    }
    
    @CacheEvict(value = "systemConfig", key = "#key")
    public void updateConfigValue(String key, String value) {
        // ...
    }
}
```

### 2. 配置热更新
- 修改配置后立即生效，无需重启应用
- 对于某些需要重启的配置（如日志级别），提供提示

---

## 🧪 测试计划

### 1. 单元测试
- 测试配置的 CRUD 操作
- 测试配置值的类型转换
- 测试配置验证逻辑

### 2. 集成测试
- 测试配置修改后的实际效果
- 测试权限控制
- 测试 CSRF 保护

### 3. UI 测试
- 测试各选项卡的切换
- 测试表单提交和验证
- 测试响应式设计

---

## 📅 开发计划

### Phase 1：基础架构（1-2 天）
- [ ] 创建数据库表和初始数据
- [ ] 编写 Liquibase 变更集
- [ ] 创建 Entity、Repository、Service

### Phase 2：核心功能（2-3 天）
- [ ] 实现配置 CRUD API
- [ ] 实现配置缓存机制
- [ ] 创建前端页面框架

### Phase 3：UI 开发（2-3 天）
- [ ] 实现选项卡切换
- [ ] 实现各配置表单
- [ ] 实现表单验证

### Phase 4：高级功能（2-3 天）
- [ ] 实现配置导入/导出
- [ ] 实现配置历史记录
- [ ] 实现配置恢复功能

### Phase 5：测试与优化（1-2 天）
- [ ] 编写单元测试
- [ ] 性能测试
- [ ] Bug 修复

**预计总工时**：8-13 天

---

## 🚀 后续扩展

### 1. 配置模板
- 预设多种配置模板（开发环境、生产环境、高安全模式等）
- 一键应用配置模板

### 2. 配置版本控制
- 记录配置变更历史
- 支持回滚到历史版本
- 对比不同版本的差异

### 3. 多环境管理
- 支持多套配置（开发、测试、生产）
- 环境间配置同步
- 配置差异对比

### 4. 智能推荐
- 根据系统使用情况推荐最优配置
- 检测配置冲突和不合理设置
- 提供配置优化建议

---

## 📝 注意事项

1. **安全性**
   - 敏感配置（如密码）需要加密存储
   - 配置变更需要审计日志
   - 防止 SQL 注入和 XSS 攻击

2. **性能**
   - 使用缓存减少数据库查询
   - 批量更新配置时使用事务
   - 避免在请求中频繁读取配置

3. **兼容性**
   - 配置变更时考虑向后兼容
   - 提供配置迁移脚本
   - 保留旧配置的默认值

4. **用户体验**
   - 提供清晰的配置说明
   - 实时验证输入合法性
   - 保存前显示变更摘要

---

## 🎉 总结

系统设置功能将为管理员提供一个强大的配置管理工具，使系统运维更加便捷和高效。通过合理的架构设计和完善的权限控制，确保配置管理的安全性和可靠性。
