# 安全检查和上线准备清单

## ✅ 已修复的安全问题

### 1. CSRF 保护（已修复）
- **问题**: 上传和删除接口禁用了 CSRF 保护
- **修复**: 重新启用 CSRF 保护，Thymeleaf 表单会自动添加 CSRF token
- **文件**: `SecurityConfig.java`

### 2. 目录遍历攻击防护（已修复）
- **问题**: 下载接口缺少路径验证
- **修复**: 在 `downloadFile()` 方法中添加路径检查，确保文件在允许的目录内
- **文件**: `FileStorageService.java`

### 3. 错误信息泄露（已修复）
- **问题**: 异常详细信息直接返回给用户
- **修复**: 
  - 捕获具体异常类型，返回友好提示
  - 配置 `server.error.include-*` 隐藏错误详情
  - 敏感信息只记录到日志
- **文件**: `FileController.java`, `application.properties`

### 4. 硬编码配置（已修复）
- **问题**: Controller 中硬编码了容量值
- **修复**: 从配置服务动态获取最大容量
- **文件**: `FileController.java`, `FileStorageService.java`

## 🔒 现有安全措施

### 认证和授权
- ✅ Spring Security 用户认证
- ✅ BCrypt 密码加密
- ✅ 基于角色的访问控制
- ✅ 安全的登出机制

### 文件安全
- ✅ 只允许上传 .zip 文件
- ✅ 文件名使用 UUID 防止冲突和猜测
- ✅ 路径规范化防止目录遍历
- ✅ 文件大小限制（100MB）
- ✅ 存储容量限制（可配置）
- ✅ 用户只能访问自己的文件

### 数据安全
- ✅ 参数化查询（JPA）
- ✅ SQL 注入防护
- ✅ 事务管理保证数据一致性

## ⚠️ 上线前建议

### 高优先级

1. **修改默认密码**
   ```properties
   # 在数据库中修改 admin 用户密码
   # 或使用更强的初始密码
   ```

2. **配置 HTTPS**
   ```properties
   server.ssl.enabled=true
   server.ssl.key-store=classpath:keystore.p12
   server.ssl.key-store-password=your_password
   server.ssl.key-store-type=PKCS12
   ```

3. **修改数据库密码**
   - 不要使用 root 账户
   - 创建专用的数据库用户
   - 授予最小权限

4. **配置文件存储路径**
   ```properties
   # 使用绝对路径，不要使用相对路径
   file.upload-dir=/var/uploads/fileservice
   ```

5. **更新下载 URL 前缀**
   ```properties
   # 根据实际域名修改
   file.download-url-prefix=https://yourdomain.com/download/
   ```

### 中优先级

6. **添加请求频率限制**
   - 防止暴力破解登录
   - 限制上传频率
   - 建议使用 Spring Rate Limiter 或 Redis

7. **添加日志监控**
   - 记录所有上传/删除操作
   - 监控异常登录尝试
   - 设置告警机制

8. **配置 CORS（如果需要）**
   ```java
   // 如果前端和后端分离，需要配置 CORS
   ```

9. **添加健康检查端点**
   ```properties
   management.endpoints.web.exposure.include=health,info
   ```

10. **数据库备份策略**
    - 定期备份数据库
    - 定期备份上传的文件

### 低优先级

11. **性能优化**
    - 添加文件列表分页
    - 使用 CDN 加速文件下载
    - 优化数据库索引

12. **功能增强**
    - 添加文件病毒扫描
    - 支持更多文件格式
    - 添加文件预览功能

13. **监控和告警**
    - 集成 Prometheus + Grafana
    - 监控磁盘使用情况
    - 监控应用性能

## 📋 上线检查清单

- [ ] 修改所有默认密码
- [ ] 配置 HTTPS 证书
- [ ] 使用专用数据库账户
- [ ] 配置文件存储为绝对路径
- [ ] 更新下载 URL 前缀为正式域名
- [ ] 测试 CSRF 保护是否正常工作
- [ ] 测试文件上传大小限制
- [ ] 测试容量限制功能
- [ ] 验证用户隔离（不能访问他人文件）
- [ ] 测试目录遍历攻击防护
- [ ] 配置生产环境日志级别（INFO 或 WARN）
- [ ] 设置数据库自动备份
- [ ] 设置文件存储备份
- [ ] 配置监控和告警
- [ ] 进行压力测试
- [ ] 准备回滚方案

## 🔧 生产环境配置示例

```properties
# 应用配置
spring.application.name=fileService
server.port=8443

# SSL 配置
server.ssl.enabled=true
server.ssl.key-store=classpath:keystore.p12
server.ssl.key-store-password=changeit
server.ssl.key-store-type=PKCS12

# 数据库配置（使用专用账户）
spring.datasource.url=jdbc:mysql://db-server:3306/fileservice?useSSL=true&serverTimezone=UTC
spring.datasource.username=fileservice_user
spring.datasource.password=STRONG_PASSWORD_HERE

# 文件配置
file.upload-dir=/data/uploads/fileservice
file.max-storage-size=1073741824
file.download-url-prefix=https://yourdomain.com/download/

# 上传限制
spring.servlet.multipart.max-file-size=100MB
spring.servlet.multipart.max-request-size=100MB

# 安全配置
server.error.include-message=never
server.error.include-stacktrace=never
server.error.include-exception=false

# 日志配置（生产环境）
logging.level.root=WARN
logging.level.net.docn.fileservice=INFO
logging.file.name=/var/log/fileservice/app.log
logging.logback.rollingpolicy.max-file-size=10MB
logging.logback.rollingpolicy.max-history=30
```

## 🚨 应急响应

### 发现安全漏洞时
1. 立即停止服务
2. 评估影响范围
3. 修复漏洞
4. 审查日志确认是否有攻击
5. 通知受影响用户（如有必要）
6. 重新启动服务
7. 持续监控

### 被攻击时
1. 封锁攻击 IP
2. 重置受影响用户密码
3. 检查数据完整性
4. 恢复备份（如需要）
5. 加强安全措施
