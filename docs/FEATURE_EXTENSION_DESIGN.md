# FileService 功能扩展设计文档

## 1. 需求概述

### 1.1 背景
当前 FileService 是一个基础的 ZIP 文件管理系统，支持单用户登录和文件管理。为满足多用户协作和企业级文件管理需求，需要进行功能扩展。

### 1.2 新增功能清单

| 功能模块 | 子功能 | 优先级 | 说明 |
|---------|--------|--------|------|
| **账号管理** | 修改密码 | P0 | 用户可自主修改登录密码 |
| | 修改邮箱 | P0 | 用户可更新联系邮箱 |
| | 查看个人信息 | P1 | 展示用户名、邮箱、创建时间等 |
| **多用户管理** | 添加用户 | P0 | 管理员创建新用户账户 |
| | 删除用户 | P0 | 管理员删除用户及其所有文件 |
| | 禁用/启用用户 | P0 | 临时禁用用户登录权限 |
| | 用户列表查询 | P1 | 分页展示所有用户信息 |
| | 角色管理 | P2 | ADMIN/USER 角色区分 |
| **文件管理增强** | 查看所有用户文件 | P0 | 管理员按用户筛选文件 |
| | 管理员下载文件 | P0 | 管理员可下载任意用户文件 |
| | 管理员删除文件 | P0 | 管理员可删除任意用户文件 |
| | 文件搜索 | P2 | 按文件名、类型、时间搜索 |
| **文件分组管理** | 用户独立目录 | P0 | 每个用户上传文件存储在独立子目录 |
| | 目录自动创建 | P0 | 用户首次上传时自动创建目录 |
| | 路径隔离 | P0 | 严格隔离不同用户文件路径 |
| **文件类型扩展** | 图片支持 | P0 | JPG、PNG、GIF、WebP |
| | 文档支持 | P1 | PDF、DOCX、XLSX、PPTX |
| | 压缩包支持 | P0 | ZIP、RAR、7Z（保持现有） |
| | 其他类型 | P2 | TXT、CSV、JSON 等文本文件 |

---

## 2. 系统架构变更

### 2.1 数据库架构调整

#### 2.1.1 用户表 (users) 扩展
```sql
ALTER TABLE users ADD COLUMN role VARCHAR(20) DEFAULT 'USER' COMMENT '角色：ADMIN/USER';
ALTER TABLE users ADD COLUMN enabled BOOLEAN DEFAULT TRUE COMMENT '是否启用';
ALTER TABLE users ADD COLUMN last_login_at TIMESTAMP NULL COMMENT '最后登录时间';
ALTER TABLE users ADD COLUMN avatar_url VARCHAR(500) NULL COMMENT '头像URL';
```

**新字段说明**：
- `role`: 用户角色（ADMIN - 管理员，USER - 普通用户）
- `enabled`: 账户状态（TRUE - 正常，FALSE - 禁用）
- `last_login_at`: 最后登录时间（用于审计）
- `avatar_url`: 用户头像（可选功能）

#### 2.1.2 文件记录表 (file_records) 调整
```sql
-- file_path 字段语义变更：从绝对路径改为相对路径
-- 原值：D:\uploads\3f16ddf1-generated.zip
-- 新值：user_1/3f16ddf1-generated.zip
```

**无需修改表结构**，但业务逻辑需要调整路径生成规则。

#### 2.1.3 新增索引
```sql
CREATE INDEX idx_users_role ON users(role);
CREATE INDEX idx_users_enabled ON users(enabled);
CREATE INDEX idx_file_records_filename ON file_records(original_filename);
CREATE INDEX idx_file_records_content_type ON file_records(content_type);
```

### 2.2 目录结构调整

#### 2.2.1 新的文件存储结构
```
uploads/
├── user_1/                    # 用户ID=1的文件目录
│   ├── abc123-photo.jpg
│   ├── def456-document.pdf
│   └── ghi789-archive.zip
├── user_2/                    # 用户ID=2的文件目录
│   ├── jkl012-image.png
│   └── mno345-data.zip
├── user_3/
│   └── ...
└── temp/                      # 临时文件目录（可选）
    └── upload_temp_xxx.zip
```

**优势**：
- 用户文件物理隔离
- 便于备份和迁移
- 简化权限控制
- 易于统计用户占用空间

### 2.3 类图设计

```
┌─────────────────────┐
│      User           │
├─────────────────────┤
│ - id: Long          │
│ - username: String  │
│ - password: String  │
│ - email: String     │
│ - role: String      │◄── 新增
│ - enabled: Boolean  │◄── 新增
│ - createdAt: LocalDateTime │
│ - updatedAt: LocalDateTime │
│ - lastLoginAt: LocalDateTime │◄── 新增
└─────────────────────┘
         ▲
         │ 1:N
┌─────────────────────┐
│   FileRecord        │
├─────────────────────┤
│ - id: Long          │
│ - userId: Long      │
│ - filename: String  │
│ - originalFilename: String │
│ - fileSize: Long    │
│ - filePath: String  │◄── 改为相对路径
│ - contentType: String │◄── 扩展类型
│ - uploadedAt: LocalDateTime │
└─────────────────────┘

┌──────────────────────────┐
│  AccountController       │◄── 新增
├──────────────────────────┤
│ + GET /account/profile   │
│ + POST /account/change-password │
│ + POST /account/change-email │
└──────────────────────────┘

┌──────────────────────────┐
│  AdminUserController     │◄── 新增
├──────────────────────────┤
│ + GET /admin/users       │
│ + POST /admin/users      │
│ + DELETE /admin/users/{id} │
│ + POST /admin/users/{id}/toggle-enabled │
└──────────────────────────┘

┌──────────────────────────┐
│  AdminFileController     │◄── 新增
├──────────────────────────┤
│ + GET /admin/files       │
│ + GET /admin/files/user/{userId} │
│ + DELETE /admin/files/{id} │
│ + GET /admin/files/{id}/download │
└──────────────────────────┘

┌──────────────────────────┐
│  FileStorageService      │◄── 改造
├──────────────────────────┤
│ + uploadFile()           │◄── 支持多类型
│ + getUserDirectory()     │◄── 新增
│ + validateFileType()     │◄── 新增
└──────────────────────────┘
```

---

## 3. 详细功能设计

### 3.1 账号管理模块

#### 3.1.1 修改密码

**接口设计**：
```
POST /account/change-password
Content-Type: application/x-www-form-urlencoded

参数：
- oldPassword: String (当前密码)
- newPassword: String (新密码，最少6位)
- confirmPassword: String (确认密码)
- _csrf: String (CSRF token)

响应：
- 成功：重定向到 /account/profile?success=password_changed
- 失败：重定向到 /account/profile?error=wrong_password
```

**业务逻辑**：
1. 验证当前密码是否正确（使用 `PasswordEncoder.matches()`）
2. 验证新密码长度 ≥ 6
3. 验证两次输入的新密码一致
4. 使用 BCrypt 加密新密码
5. 更新数据库
6. 强制退出登录（可选安全策略）

**前端页面**：`templates/account/profile.html`
- 显示当前用户名、邮箱
- 密码修改表单
- 邮箱修改表单

#### 3.1.2 修改邮箱

**接口设计**：
```
POST /account/change-email

参数：
- email: String (新邮箱，需符合邮箱格式)
- _csrf: String

响应：
- 成功：重定向到 /account/profile?success=email_changed
- 失败：重定向到 /account/profile?error=invalid_email
```

**业务逻辑**：
1. 验证邮箱格式（正则表达式）
2. 检查邮箱是否已被其他用户使用
3. 更新数据库
4. 记录操作日志（可选）

#### 3.1.3 查看个人信息

**接口设计**：
```
GET /account/profile

Model 属性：
- user: User (当前用户对象)
- fileCount: Long (用户上传的文件数量)
- usedStorage: String (已用容量，格式化)
- message: String (操作成功消息)
- error: String (错误消息)
```

**页面内容**：
- 用户名（不可修改）
- 邮箱（可修改）
- 角色（只读显示）
- 账户状态（只读显示）
- 创建时间
- 最后登录时间
- 文件统计信息

### 3.2 多用户管理模块（管理员专用）

#### 3.2.1 用户列表查询

**接口设计**：
```
GET /admin/users

请求参数：
- page: Integer (页码，默认1)
- size: Integer (每页大小，默认10)
- keyword: String (搜索关键词，可选)
- role: String (角色过滤，可选)
- enabled: Boolean (状态过滤，可选)

Model 属性：
- users: Page<User> (分页用户列表)
- totalPages: Integer
- currentPage: Integer
```

**权限控制**：
- 仅 ADMIN 角色可访问
- 在 Controller 中通过 `@PreAuthorize("hasRole('ADMIN')")` 或手动检查

#### 3.2.2 添加用户

**接口设计**：
```
POST /admin/users

参数：
- username: String (用户名，唯一)
- password: String (初始密码)
- email: String (邮箱，可选)
- role: String (ADMIN/USER，默认USER)
- enabled: Boolean (是否启用，默认true)
- _csrf: String

响应：
- 成功：重定向到 /admin/users?success=user_created
- 失败：重定向到 /admin/users?error=username_exists
```

**业务逻辑**：
1. 验证用户名唯一性
2. 验证用户名格式（字母数字下划线，3-20位）
3. 验证密码强度（最少6位）
4. 验证邮箱格式（如果提供）
5. BCrypt 加密密码
6. 创建用户记录
7. 自动创建用户文件目录：`uploads/user_{id}/`

#### 3.2.3 删除用户

**接口设计**：
```
DELETE /admin/users/{id}
或
POST /admin/users/{id}/delete (带 CSRF token)

响应：
- 成功：重定向到 /admin/users?success=user_deleted
- 失败：重定向到 /admin/users?error=cannot_delete_self
```

**业务逻辑**：
1. 检查是否为当前登录用户（不能删除自己）
2. 查询用户所有文件记录
3. 物理删除用户目录下所有文件
4. 删除数据库中的文件记录
5. 删除用户目录
6. 删除用户记录

**⚠️ 警告**：此操作不可逆，建议添加二次确认

#### 3.2.4 禁用/启用用户

**接口设计**：
```
POST /admin/users/{id}/toggle-enabled

响应：
- 成功：重定向到 /admin/users?success=user_toggled
- 失败：返回错误消息
```

**业务逻辑**：
1. 切换 `enabled` 字段状态
2. 如果禁用用户，使其当前 Session 失效（可选）
3. 被禁用用户下次登录时拒绝访问

**登录拦截**：
在 `CustomUserDetailsService` 中检查：
```java
if (!user.isEnabled()) {
    throw new DisabledException("账户已被禁用");
}
```

### 3.3 文件管理增强模块

#### 3.3.1 管理员查看所有文件

**接口设计**：
```
GET /admin/files

请求参数：
- userId: Long (按用户筛选，可选)
- page: Integer
- size: Integer
- keyword: String (文件名搜索)
- contentType: String (文件类型过滤)

Model 属性：
- files: Page<FileRecord>
- users: List<User> (用于下拉选择)
- selectedUserId: Long
```

**页面布局**：
- 顶部：用户筛选下拉框 + 搜索框
- 中部：文件列表表格
  - 文件名
  - 所属用户
  - 文件大小
  - 上传时间
  - 操作（下载、删除）

#### 3.3.2 管理员下载文件

**接口设计**：
```
GET /admin/files/{id}/download

权限：仅 ADMIN
响应：文件流（与普通下载相同）
```

**实现**：复用现有的下载逻辑，仅需添加权限检查

#### 3.3.3 管理员删除文件

**接口设计**：
```
POST /admin/files/{id}/delete

权限：仅 ADMIN
响应：重定向回文件列表
```

**业务逻辑**：
1. 查询文件记录
2. 删除物理文件（从对应用户目录）
3. 删除数据库记录
4. 无需验证文件所有权（管理员特权）

### 3.4 文件分组管理模块

#### 3.4.1 用户目录自动创建

**时机**：
1. 用户注册时自动创建
2. 用户首次上传文件时创建（懒加载方式）

**实现**：
```java
@Service
public class FileStorageService {
    
    public Path getUserDirectory(Long userId) {
        Path userDir = rootLocation.resolve("user_" + userId).normalize();
        
        if (!Files.exists(userDir)) {
            try {
                Files.createDirectories(userDir);
                log.info("为用户 {} 创建目录: {}", userId, userDir);
            } catch (IOException e) {
                throw new RuntimeException("无法创建用户目录", e);
            }
        }
        
        return userDir;
    }
}
```

#### 3.4.2 文件路径生成规则

**旧规则**：
```
filePath = uploads/uuid_originalname.zip
```

**新规则**：
```
filePath = uploads/user_{userId}/uuid_originalname.ext
```

**示例**：
- 用户ID=1上传 `photo.jpg`
- 生成文件名：`a1b2c3d4-e5f6-photo.jpg`
- 存储路径：`uploads/user_1/a1b2c3d4-e5f6-photo.jpg`
- 数据库保存：`user_1/a1b2c3d4-e5f6-photo.jpg`（相对路径）

#### 3.4.3 路径安全检查增强

```java
// 确保文件路径在用户自己的目录内
Path userDir = getUserDirectory(userId);
Path targetPath = userDir.resolve(uniqueFilename).normalize();

if (!targetPath.startsWith(userDir)) {
    throw new IOException("非法的文件路径");
}
```

### 3.5 文件类型扩展模块

#### 3.5.1 支持的文件类型配置

**配置文件新增**：
```properties
# 允许的文件类型（MIME类型）
file.allowed-types=image/jpeg,image/png,image/gif,image/webp,application/zip,application/pdf,application/msword,application/vnd.openxmlformats-officedocument.wordprocessingml.document,text/plain

# 允许的文件扩展名
file.allowed-extensions=.jpg,.jpeg,.png,.gif,.webp,.zip,.pdf,.doc,.docx,.txt,.csv
```

**配置类扩展**：
```java
@Component
@ConfigurationProperties(prefix = "file")
public class FileStorageProperties {
    private String uploadDir;
    private long maxStorageSize;
    private String downloadUrlPrefix;
    private List<String> allowedTypes;      // 新增
    private List<String> allowedExtensions; // 新增
    
    // getters and setters...
}
```

#### 3.5.2 文件类型验证逻辑

```java
private void validateFileType(MultipartFile file) {
    String originalFilename = file.getOriginalFilename();
    String contentType = file.getContentType();
    
    // 1. 检查扩展名
    String extension = getFileExtension(originalFilename).toLowerCase();
    if (!fileStorageProperties.getAllowedExtensions().contains(extension)) {
        throw new IllegalArgumentException("不支持的文件类型: " + extension);
    }
    
    // 2. 检查 MIME 类型（可选，更严格）
    if (contentType != null && !fileStorageProperties.getAllowedTypes().contains(contentType)) {
        log.warn("文件MIME类型不在白名单中: {}", contentType);
        // 可以选择抛出异常或仅记录警告
    }
    
    // 3. 检查文件大小
    if (file.getSize() > fileStorageProperties.getMaxFileSize()) {
        throw new IllegalArgumentException("文件大小超过限制");
    }
}

private String getFileExtension(String filename) {
    int lastDotIndex = filename.lastIndexOf(".");
    if (lastDotIndex == -1) {
        return "";
    }
    return filename.substring(lastDotIndex);
}
```

#### 3.5.3 文件类型分类展示

**前端优化**：
根据文件类型显示不同图标：
- 📷 图片文件（jpg/png/gif）
- 📄 文档文件（pdf/docx）
- 📦 压缩文件（zip/rar）
- 📝 文本文件（txt/csv）

**CSS 样式**：
```css
.file-icon.image { color: #4CAF50; }
.file-icon.document { color: #2196F3; }
.file-icon.archive { color: #FF9800; }
.file-icon.text { color: #9E9E9E; }
```

---

## 4. 安全设计

### 4.1 权限控制矩阵

| 功能 | 未登录 | USER | ADMIN |
|------|--------|------|-------|
| 登录/登出 | ✅ | ✅ | ✅ |
| 修改个人信息 | ❌ | ✅ | ✅ |
| 查看个人文件 | ❌ | ✅（仅自己的） | ✅（所有用户的） |
| 上传文件 | ❌ | ✅ | ✅ |
| 删除个人文件 | ❌ | ✅（仅自己的） | ✅（所有用户的） |
| 下载公开文件 | ✅ | ✅ | ✅ |
| 用户管理 | ❌ | ❌ | ✅ |
| 查看所有文件 | ❌ | ❌ | ✅ |

### 4.2 角色权限实现

#### 4.2.1 Spring Security 配置
```java
@Bean
public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
    http
        .addFilterBefore(captchaFilter, UsernamePasswordAuthenticationFilter.class)
        .authorizeHttpRequests(auth -> auth
            // 公开访问
            .requestMatchers("/login", "/captcha", "/css/**", "/js/**").permitAll()
            .requestMatchers("/download/**").permitAll()
            
            // 管理员专属
            .requestMatchers("/admin/**").hasRole("ADMIN")
            
            // 需要登录
            .requestMatchers("/files", "/upload", "/delete/**").authenticated()
            .requestMatchers("/account/**").authenticated()
            
            .anyRequest().authenticated()
        )
        .formLogin(form -> form
            .loginPage("/login")
            .defaultSuccessUrl("/", true) // 根据角色跳转不同页面
            .successHandler(customAuthenticationSuccessHandler) // 自定义跳转逻辑
            .permitAll()
        );
    
    return http.build();
}
```

#### 4.2.2 自定义认证成功处理器
```java
@Component
public class CustomAuthenticationSuccessHandler extends SimpleUrlRedirectStrategy {
    
    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, 
                                       HttpServletResponse response,
                                       Authentication authentication) {
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        
        if (userDetails.isAdmin()) {
            response.sendRedirect("/admin/dashboard");
        } else {
            response.sendRedirect("/files");
        }
    }
}
```

### 4.3 跨用户文件访问防护

**场景**：用户A尝试访问用户B的文件

**防护措施**：
1. **路径隔离**：每个用户文件存储在独立目录
2. **权限校验**：下载/删除时验证文件归属
3. **管理员例外**：ADMIN 角色可跨用户操作

**代码示例**：
```java
public void deleteFile(Long fileId, Long currentUserId, String currentUserRole) {
    FileRecord fileRecord = fileRecordRepository.findById(fileId)
        .orElseThrow(() -> new RuntimeException("文件不存在"));
    
    // 非管理员必须是自己上传的文件
    if (!"ADMIN".equals(currentUserRole) && !fileRecord.getUserId().equals(currentUserId)) {
        throw new RuntimeException("无权删除此文件");
    }
    
    // 执行删除...
}
```

### 4.4 文件上传安全

#### 4.4.1 文件内容验证
```java
// 防止恶意文件伪装（如将.exe改名为.jpg）
private void validateFileContent(MultipartFile file) {
    try (InputStream is = file.getInputStream()) {
        byte[] header = new byte[16];
        is.read(header);
        
        String fileType = detectFileType(header);
        String expectedType = getFileExtension(file.getOriginalFilename());
        
        if (!isCompatible(fileType, expectedType)) {
            throw new IllegalArgumentException("文件内容与扩展名不匹配");
        }
    }
}
```

#### 4.4.2 防病毒扫描（可选）
集成 ClamAV 或其他杀毒引擎：
```java
@Autowired
private AntivirusService antivirusService;

public FileRecord uploadFile(MultipartFile file, Long userId) {
    // 1. 病毒扫描
    if (!antivirusService.scan(file)) {
        throw new IllegalArgumentException("文件包含病毒");
    }
    
    // 2. 继续上传流程...
}
```

---

## 5. 前端页面设计

### 5.1 新增页面清单

| 页面路径 | 模板文件 | 访问权限 | 说明 |
|---------|---------|---------|------|
| `/account/profile` | `account/profile.html` | 登录用户 | 个人信息管理 |
| `/admin/dashboard` | `admin/dashboard.html` | ADMIN | 管理员仪表盘 |
| `/admin/users` | `admin/users.html` | ADMIN | 用户管理 |
| `/admin/files` | `admin/files.html` | ADMIN | 全局文件管理 |

### 5.2 页面原型设计

#### 5.2.1 个人信息页面 (account/profile.html)
```
┌─────────────────────────────────────┐
│  📁 文件服务器 - 个人中心            │
├─────────────────────────────────────┤
│                                     │
│  👤 基本信息                         │
│  ┌─────────────────────────────┐   │
│  │ 用户名: admin               │   │
│  │ 角  色: 管理员              │   │
│  │ 状  态: ✅ 正常             │   │
│  │ 邮  箱: admin@example.com   │   │
│  │ 创建时间: 2026-01-01        │   │
│  └─────────────────────────────┘   │
│                                     │
│  ✏️ 修改邮箱                         │
│  ┌─────────────────────────────┐   │
│  │ [新邮箱输入框]  [保存]      │   │
│  └─────────────────────────────┘   │
│                                     │
│  🔒 修改密码                         │
│  ┌─────────────────────────────┐   │
│  │ 当前密码: [******]          │   │
│  │ 新密码:   [******]          │   │
│  │ 确认密码: [******]          │   │
│  │ [修改密码]                  │   │
│  └─────────────────────────────┘   │
│                                     │
│  📊 文件统计                         │
│  ┌─────────────────────────────┐   │
│  │ 文件数量: 15                │   │
│  │ 已用容量: 256 MB / 1 GB     │   │
│  └─────────────────────────────┘   │
│                                     │
└─────────────────────────────────────┘
```

#### 5.2.2 管理员用户管理页面 (admin/users.html)
```
┌──────────────────────────────────────────────┐
│  🛡️ 管理员控制台 - 用户管理                   │
├──────────────────────────────────────────────┤
│                                              │
│  [+ 新建用户]  [🔍 搜索用户...]              │
│                                              │
│  ┌────────────────────────────────────────┐ │
│  │ ID | 用户名 | 邮箱 | 角色 | 状态 | 操作│ │
│  ├────────────────────────────────────────┤ │
│  │ 1  | admin  | ...  | ADMIN| ✅   |编辑│ │
│  │ 2  | user1  | ...  | USER | ✅   |编辑│ │
│  │ 3  | user2  | ...  | USER | ❌   |编辑│ │
│  └────────────────────────────────────────┘ │
│                                              │
│  分页: < 1 2 3 >                             │
│                                              │
└──────────────────────────────────────────────┘

【新建用户模态框】
┌─────────────────────────────┐
│  创建新用户                  │
├─────────────────────────────┤
│  用户名: [__________]       │
│  密  码: [__________]       │
│  邮  箱: [__________]       │
│  角  色: (○)USER (●)ADMIN  │
│  状  态: [✅ 启用]          │
│                             │
│  [取消]  [创建]             │
└─────────────────────────────┘
```

#### 5.2.3 管理员文件管理页面 (admin/files.html)
```
┌──────────────────────────────────────────────────┐
│  🛡️ 管理员控制台 - 文件管理                       │
├──────────────────────────────────────────────────┤
│                                                  │
│  用户筛选: [全部用户 ▼]  类型: [全部 ▼]         │
│  🔍 搜索文件名...                                │
│                                                  │
│  ┌────────────────────────────────────────────┐ │
│  │ 文件名 | 所属用户 | 大小 | 时间 | 操作     │ │
│  ├────────────────────────────────────────────┤ │
│  │ 📷 photo.jpg | user1 | 2MB | 2026-06-01   │ │
│  │    [下载] [删除]                           │ │
│  │ 📄 report.pdf | user2 | 5MB | 2026-06-02  │ │
│  │    [下载] [删除]                           │ │
│  └────────────────────────────────────────────┘ │
│                                                  │
│  分页: < 1 2 3 ... 10 >                          │
│                                                  │
└──────────────────────────────────────────────────┘
```

### 5.3 导航菜单调整

#### 5.3.1 普通用户菜单
```
┌─────────────────────┐
│ 📁 文件服务器       │
├─────────────────────┤
│ 📂 我的文件         │
│ 👤 个人中心         │
│ 🚪 退出登录         │
└─────────────────────┘
```

#### 5.3.2 管理员菜单
```
┌─────────────────────┐
│ 🛡️ 管理控制台       │
├─────────────────────┤
│ 📊 仪表盘           │
│ 👥 用户管理         │
│ 📁 文件管理         │
│ 📂 我的文件         │
│ 👤 个人中心         │
│ 🚪 退出登录         │
└─────────────────────┘
```

---

## 6. API 接口汇总

### 6.1 账号管理接口

| 方法 | 路径 | 权限 | 说明 |
|------|------|------|------|
| GET | `/account/profile` | 登录用户 | 查看个人信息 |
| POST | `/account/change-password` | 登录用户 | 修改密码 |
| POST | `/account/change-email` | 登录用户 | 修改邮箱 |

### 6.2 用户管理接口（ADMIN）

| 方法 | 路径 | 权限 | 说明 |
|------|------|------|------|
| GET | `/admin/users` | ADMIN | 用户列表（分页） |
| POST | `/admin/users` | ADMIN | 创建用户 |
| DELETE | `/admin/users/{id}` | ADMIN | 删除用户 |
| POST | `/admin/users/{id}/toggle-enabled` | ADMIN | 禁用/启用用户 |
| GET | `/admin/users/{id}` | ADMIN | 用户详情 |

### 6.3 文件管理接口

| 方法 | 路径 | 权限 | 说明 |
|------|------|------|------|
| GET | `/files` | 登录用户 | 个人文件列表 |
| POST | `/upload` | 登录用户 | 上传文件 |
| POST | `/delete/{id}` | 登录用户 | 删除个人文件 |
| GET | `/admin/files` | ADMIN | 全局文件列表 |
| GET | `/admin/files/user/{userId}` | ADMIN | 指定用户文件 |
| POST | `/admin/files/{id}/delete` | ADMIN | 管理员删除文件 |
| GET | `/admin/files/{id}/download` | ADMIN | 管理员下载文件 |

---

## 7. 数据库初始化方案

### 7.1 说明

由于项目尚未上线，无需数据迁移。新项目启动时将直接创建新的数据库表结构和目录结构。

**初始化步骤**：
1. Liquibase 自动执行变更集创建表结构
2. 应用启动时自动创建默认管理员账户
3. 首次上传文件时自动创建用户目录

### 7.2 Liquibase 变更集

**新增变更集文件**：`db/changelog/changes/003-add-user-fields.xml`

```xml
<?xml version="1.0" encoding="UTF-8"?>
<databaseChangeLog xmlns="http://www.liquibase.org/xml/ns/dbchangelog">
    
    <changeSet id="003-add-user-fields" author="fileService">
        <!-- 添加角色字段 -->
        <addColumn tableName="users">
            <column name="role" type="VARCHAR(20)" defaultValue="USER">
                <constraints nullable="false"/>
            </column>
        </addColumn>
        
        <!-- 添加启用状态字段 -->
        <addColumn tableName="users">
            <column name="enabled" type="BOOLEAN" defaultValueBoolean="true">
                <constraints nullable="false"/>
            </column>
        </addColumn>
        
        <!-- 添加最后登录时间字段 -->
        <addColumn tableName="users">
            <column name="last_login_at" type="TIMESTAMP">
                <constraints nullable="true"/>
            </column>
        </addColumn>
        
        <!-- 更新默认管理员为 ADMIN 角色 -->
        <update tableName="users">
            <column name="role" value="ADMIN"/>
            <where>username = 'admin'</where>
        </update>
    </changeSet>
    
    <changeSet id="004-create-indexes" author="fileService">
        <!-- 创建新索引 -->
        <createIndex tableName="users" indexName="idx_users_role">
            <column name="role"/>
        </createIndex>
        
        <createIndex tableName="users" indexName="idx_users_enabled">
            <column name="enabled"/>
        </createIndex>
        
        <createIndex tableName="file_records" indexName="idx_file_records_filename">
            <column name="original_filename"/>
        </createIndex>
    </changeSet>
    
</databaseChangeLog>
```

---

## 8. 测试策略

### 8.1 单元测试

#### 8.1.1 账号管理测试
```java
@SpringBootTest
class AccountServiceTest {
    
    @Autowired
    private AccountService accountService;
    
    @Test
    void testChangePassword_Success() {
        // 准备
        Long userId = 1L;
        String oldPassword = "admin123";
        String newPassword = "newPass123";
        
        // 执行
        accountService.changePassword(userId, oldPassword, newPassword);
        
        // 验证
        assertTrue(passwordEncoder.matches(newPassword, 
            userRepository.findById(userId).get().getPassword()));
    }
    
    @Test
    void testChangePassword_WrongOldPassword() {
        assertThrows(IllegalArgumentException.class, () -> {
            accountService.changePassword(1L, "wrongPass", "newPass");
        });
    }
}
```

#### 8.1.2 用户管理测试
```java
@Test
void testCreateUser_Success() {
    CreateUserRequest request = new CreateUserRequest();
    request.setUsername("testuser");
    request.setPassword("test123");
    request.setEmail("test@example.com");
    request.setRole("USER");
    
    User user = userService.createUser(request);
    
    assertNotNull(user.getId());
    assertEquals("USER", user.getRole());
    assertTrue(Files.exists(Paths.get("uploads/user_" + user.getId())));
}

@Test
void testDeleteUser_RemovesFilesAndDirectory() {
    Long userId = 2L;
    
    userService.deleteUser(userId);
    
    assertFalse(userRepository.existsById(userId));
    assertFalse(Files.exists(Paths.get("uploads/user_" + userId)));
}
```

### 8.2 集成测试

#### 8.2.1 文件上传测试
```java
@Test
void testUploadFile_CreatesUserDirectory() throws Exception {
    MockMultipartFile file = new MockMultipartFile(
        "file", "test.jpg", "image/jpeg", "fake image data".getBytes()
    );
    
    mockMvc.perform(multipart("/upload")
            .file(file)
            .with(csrf()))
        .andExpect(status().is3xxRedirection())
        .andExpect(redirectedUrl("/files"));
    
    // 验证文件已保存到用户目录
    assertTrue(Files.exists(Paths.get("uploads/user_1/test.jpg")));
}
```

#### 8.2.2 权限控制测试
```java
@Test
void testNormalUser_CannotAccessAdminPages() throws Exception {
    mockMvc.perform(get("/admin/users")
            .with(user("normaluser").roles("USER")))
        .andExpect(status().isForbidden());
}

@Test
void testAdmin_CanAccessAdminPages() throws Exception {
    mockMvc.perform(get("/admin/users")
            .with(user("admin").roles("ADMIN")))
        .andExpect(status().isOk());
}
```

### 8.3 安全测试

| 测试场景 | 预期结果 |
|---------|---------|
| 用户A尝试访问用户B的文件 | 返回 403 Forbidden |
| 普通用户访问 /admin/* | 返回 403 Forbidden |
| 禁用用户尝试登录 | 返回 "账户已被禁用" |
| 上传禁止的文件类型（.exe） | 返回 "不支持的文件类型" |
| 路径遍历攻击（../../../etc/passwd） | 返回 "非法的文件路径" |
| CSRF 攻击（不带 token） | 返回 403 Forbidden |

---

## 9. 性能与优化

### 9.1 数据库查询优化

#### 9.1.1 分页查询
```java
@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    
    @Query("SELECT u FROM User u WHERE (:keyword IS NULL OR u.username LIKE %:keyword%)")
    Page<User> findByKeyword(@Param("keyword") String keyword, Pageable pageable);
}
```

#### 9.1.2 批量删除优化
```java
@Transactional
public void deleteUserBatch(List<Long> userIds) {
    // 1. 批量查询文件
    List<FileRecord> files = fileRecordRepository.findByUserIdIn(userIds);
    
    // 2. 批量删除物理文件（并行处理）
    files.parallelStream().forEach(file -> {
        try {
            Files.deleteIfExists(Paths.get(file.getFilePath()));
        } catch (IOException e) {
            log.error("删除文件失败: {}", file.getFilename(), e);
        }
    });
    
    // 3. 批量删除数据库记录
    fileRecordRepository.deleteByUserIdIn(userIds);
    userRepository.deleteAllById(userIds);
}
```

### 9.2 文件操作优化

#### 9.2.1 异步文件删除
```java
@Async
public CompletableFuture<Void> deleteFileAsync(Long fileId) {
    // 异步删除物理文件，不阻塞主线程
    return CompletableFuture.runAsync(() -> {
        // 删除逻辑...
    });
}
```

#### 9.2.2 文件列表缓存
```java
@Cacheable(value = "userFiles", key = "#userId")
public List<FileRecord> getUserFiles(Long userId) {
    return fileRecordRepository.findByUserIdOrderByUploadedAtDesc(userId);
}

@CacheEvict(value = "userFiles", key = "#userId")
public void invalidateUserFilesCache(Long userId) {
    // 上传/删除文件后清除缓存
}
```

### 9.3 前端性能优化

- **懒加载**：文件列表滚动加载
- **图片缩略图**：生成小尺寸预览图
- **CDN 加速**：静态资源使用 CDN
- **Gzip 压缩**：启用 HTTP 压缩

---

## 10. 部署注意事项

### 10.1 配置文件更新

**application.properties 新增配置**：
```properties
# 文件类型配置
file.allowed-extensions=.jpg,.jpeg,.png,.gif,.webp,.zip,.pdf,.doc,.docx,.txt,.csv
file.allowed-types=image/jpeg,image/png,image/gif,image/webp,application/zip,application/pdf

# 单文件大小限制（10MB）
file.max-file-size=10485760

# 管理员配置
admin.default-password=admin123
admin.email=admin@example.com
```

### 10.2 环境变量敏感信息

**生产环境建议使用环境变量**：
```properties
spring.datasource.password=${DB_PASSWORD}
file.admin-default-password=${ADMIN_PASSWORD}
```

### 10.3 目录权限设置

```bash
# Linux 环境
chmod 750 uploads/
chown www-data:www-data uploads/

# Windows 环境
# 确保运行用户对 uploads/ 有读写权限
```

### 10.4 备份策略

**每日定时备份**：
```bash
#!/bin/bash
# backup.sh

DATE=$(date +%Y%m%d_%H%M%S)

# 备份数据库
mysqldump -u root -p${DB_PASSWORD} fileservice > backups/db_${DATE}.sql

# 备份文件
tar -czf backups/uploads_${DATE}.tar.gz uploads/

# 保留最近7天的备份
find backups/ -name "*.sql" -mtime +7 -delete
find backups/ -name "*.tar.gz" -mtime +7 -delete
```

---

## 11. 风险评估与应对

### 11.1 技术风险

| 风险 | 影响 | 概率 | 应对措施 |
|------|------|------|---------|
| 数据库初始化失败 | 高 | 低 | Liquibase 测试 + 日志监控 |
| 文件路径泄露 | 高 | 中 | 严格路径校验 + 日志审计 |
| 权限绕过漏洞 | 高 | 低 | 多层权限检查 + 安全测试 |
| 大文件上传超时 | 中 | 中 | 分块上传 + 增加超时时间 |
| 磁盘空间耗尽 | 中 | 中 | 监控告警 + 自动清理策略 |

### 11.2 业务风险

| 风险 | 应对措施 |
|------|---------|
| 管理员误删用户 | 二次确认 + 软删除（标记删除，保留30天） |
| 用户上传违规文件 | 文件类型白名单 + 内容审核（可选） |
| 密码泄露 | 强制密码复杂度 + 定期更换提醒 |

---

## 12. 后续扩展方向

### 12.1 短期规划（1-3个月）
- [ ] 文件分享链接（带有效期和访问次数限制）
- [ ] 文件回收站（删除后保留30天）
- [ ] 操作日志审计
- [ ] 文件预览功能（图片、PDF）

### 12.2 中期规划（3-6个月）
- [ ] 文件夹层级管理
- [ ] 文件版本控制
- [ ] 批量操作（批量上传、批量下载）
- [ ] 文件搜索（全文检索）

### 12.3 长期规划（6-12个月）
- [ ] 微服务化拆分
- [ ] 分布式文件存储（MinIO/Ceph）
- [ ] CDN 加速
- [ ] 移动端 APP

---

## 附录

### A. 参考资料
- [Spring Security 官方文档](https://spring.io/projects/spring-security)
- [Thymeleaf 模板引擎](https://www.thymeleaf.org/)
- [Liquibase 数据库迁移](https://docs.liquibase.com/)

### B. 术语表
- **RBAC**: Role-Based Access Control（基于角色的访问控制）
- **CSRF**: Cross-Site Request Forgery（跨站请求伪造）
- **BCrypt**: 一种安全的密码哈希算法
- **MIME**: Multipurpose Internet Mail Extensions（媒体类型）

---

**文档版本**: 1.0  
**最后更新**: 2026-06-11  
**作者**: AI Assistant
