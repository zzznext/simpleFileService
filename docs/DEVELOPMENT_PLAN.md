# FileService 功能扩展开发计划

## 📋 项目概览

### 项目目标
为 FileService 添加账号管理、多用户管理、文件管理增强、文件分组管理和文件类型扩展等功能，将其从单用户文件管理系统升级为多用户企业级文件管理平台。

### 开发周期
**预计总工期**: 4-6 周（20-30 个工作日）

### 团队角色
- **后端开发**: 1-2 人
- **前端开发**: 1 人（可选，如需要精美UI）
- **测试工程师**: 1 人（可兼任）

---

## 🎯 里程碑规划

### 里程碑 1: 基础架构改造（第 1 周）
**目标**: 完成数据库扩展、目录结构调整、文件迁移

| 任务 | 工时 | 负责人 | 状态 |
|------|------|--------|------|
| 数据库 schema 变更 | 1 天 | 后端 | ⏳ |
| Liquibase 变更集编写 | 0.5 天 | 后端 | ⏳ |
| 文件存储目录重构 | 1 天 | 后端 | ⏳ |
| 数据迁移脚本开发与测试 | 2 天 | 后端 | ⏳ |
| 配置类扩展 | 0.5 天 | 后端 | ⏳ |

**交付物**:
- ✅ 新的数据库表结构
- ✅ 文件迁移工具
- ✅ 更新后的配置文件

**验收标准**:
- [ ] 数据库字段添加成功
- [ ] 现有文件成功迁移到用户子目录
- [ ] 应用正常启动无错误

---

### 里程碑 2: 账号管理模块（第 2 周）
**目标**: 实现用户个人信息管理功能

| 任务 | 工时 | 负责人 | 状态 |
|------|------|--------|------|
| AccountController 开发 | 1 天 | 后端 | ⏳ |
| 修改密码功能 | 1 天 | 后端 | ⏳ |
| 修改邮箱功能 | 0.5 天 | 后端 | ⏳ |
| 个人信息页面开发 | 1.5 天 | 前端 | ⏳ |
| 单元测试编写 | 1 天 | 测试 | ⏳ |

**交付物**:
- ✅ `/account/profile` 页面
- ✅ 密码修改接口
- ✅ 邮箱修改接口

**验收标准**:
- [ ] 用户可以成功修改密码
- [ ] 用户可以成功修改邮箱
- [ ] 旧密码验证正确
- [ ] 邮箱格式校验有效
- [ ] 页面响应式布局正常

---

### 里程碑 3: 多用户管理模块（第 3 周）
**目标**: 实现管理员用户管理功能

| 任务 | 工时 | 负责人 | 状态 |
|------|------|--------|------|
| AdminUserController 开发 | 1.5 天 | 后端 | ⏳ |
| 用户列表查询（分页） | 1 天 | 后端 | ⏳ |
| 创建用户功能 | 1 天 | 后端 | ⏳ |
| 删除用户功能 | 1 天 | 后端 | ⏳ |
| 禁用/启用用户功能 | 0.5 天 | 后端 | ⏳ |
| 用户管理页面开发 | 2 天 | 前端 | ⏳ |
| 角色权限控制实现 | 1 天 | 后端 | ⏳ |

**交付物**:
- ✅ `/admin/users` 管理页面
- ✅ 用户 CRUD 接口
- ✅ 角色权限拦截器

**验收标准**:
- [ ] 管理员可以查看用户列表（分页）
- [ ] 管理员可以创建新用户
- [ ] 管理员可以删除用户（含文件清理）
- [ ] 管理员可以禁用/启用用户
- [ ] 被禁用用户无法登录
- [ ] 普通用户无法访问管理页面

---

### 里程碑 4: 文件管理增强（第 4 周）
**目标**: 实现管理员全局文件管理

| 任务 | 工时 | 负责人 | 状态 |
|------|------|--------|------|
| AdminFileController 开发 | 1.5 天 | 后端 | ⏳ |
| 全局文件列表查询 | 1 天 | 后端 | ⏳ |
| 按用户筛选文件 | 0.5 天 | 后端 | ⏳ |
| 管理员下载文件 | 0.5 天 | 后端 | ⏳ |
| 管理员删除文件 | 0.5 天 | 后端 | ⏳ |
| 文件管理页面开发 | 2 天 | 前端 | ⏳ |
| 文件搜索功能 | 1 天 | 后端 | ⏳ |

**交付物**:
- ✅ `/admin/files` 管理页面
- ✅ 全局文件查询接口
- ✅ 文件搜索功能

**验收标准**:
- [ ] 管理员可以查看所有用户的文件
- [ ] 可以按用户筛选文件
- [ ] 可以按文件名搜索
- [ ] 管理员可以下载任意用户文件
- [ ] 管理员可以删除任意用户文件
- [ ] 分页功能正常

---

### 里程碑 5: 文件类型扩展（第 5 周）
**目标**: 支持多种文件类型上传

| 任务 | 工时 | 负责人 | 状态 |
|------|------|--------|------|
| 文件类型配置扩展 | 0.5 天 | 后端 | ⏳ |
| 文件类型验证逻辑 | 1 天 | 后端 | ⏳ |
| 支持图片文件（JPG/PNG/GIF） | 1 天 | 后端 | ⏳ |
| 支持文档文件（PDF/DOCX） | 1 天 | 后端 | ⏳ |
| 前端文件类型图标展示 | 1 天 | 前端 | ⏳ |
| 上传文件大小限制调整 | 0.5 天 | 后端 | ⏳ |
| 集成测试 | 1 天 | 测试 | ⏳ |

**交付物**:
- ✅ 支持的文件类型白名单
- ✅ 文件类型验证服务
- ✅ 前端文件图标分类

**验收标准**:
- [ ] 可以上传 JPG/PNG/GIF 图片
- [ ] 可以上传 PDF/DOCX 文档
- [ ] 禁止上传未授权的文件类型（如 .exe）
- [ ] 文件列表显示对应类型图标
- [ ] 文件大小限制生效

---

### 里程碑 6: 测试与优化（第 6 周）
**目标**: 全面测试、性能优化、文档完善

| 任务 | 工时 | 负责人 | 状态 |
|------|------|--------|------|
| 单元测试补充 | 2 天 | 测试 | ⏳ |
| 集成测试执行 | 2 天 | 测试 | ⏳ |
| 安全测试（渗透测试） | 1.5 天 | 测试 | ⏳ |
| 性能测试与优化 | 1.5 天 | 后端 | ⏳ |
| Bug 修复 | 2 天 | 全员 | ⏳ |
| 用户手册编写 | 1 天 | 文档 | ⏳ |
| 部署文档更新 | 0.5 天 | 文档 | ⏳ |

**交付物**:
- ✅ 测试报告
- ✅ 性能优化报告
- ✅ 用户手册
- ✅ 部署指南

**验收标准**:
- [ ] 单元测试覆盖率 ≥ 80%
- [ ] 所有集成测试通过
- [ ] 安全漏洞扫描无高危问题
- [ ] 页面加载时间 < 2 秒
- [ ] 并发用户数支持 ≥ 50

---

## 📝 详细任务分解

### Phase 1: 数据库与架构改造

#### Task 1.1: 数据库 Schema 变更
**优先级**: P0  
**依赖**: 无

**工作内容**:
1. 编写 Liquibase 变更集 `003-add-user-fields.xml`
   - 添加 `role` 字段（VARCHAR(20), DEFAULT 'USER'）
   - 添加 `enabled` 字段（BOOLEAN, DEFAULT TRUE）
   - 添加 `last_login_at` 字段（TIMESTAMP, NULLABLE）
2. 编写索引变更集 `004-create-indexes.xml`
   - 创建 `idx_users_role`
   - 创建 `idx_users_enabled`
   - 创建 `idx_file_records_filename`
3. 更新默认管理员角色为 ADMIN

**技术要点**:
```xml
<changeSet id="003-add-user-fields" author="fileService">
    <addColumn tableName="users">
        <column name="role" type="VARCHAR(20)" defaultValue="USER">
            <constraints nullable="false"/>
        </column>
    </addColumn>
    <!-- 其他字段... -->
</changeSet>
```

**验收标准**:
- [ ] Liquibase 自动执行成功
- [ ] 新字段出现在 users 表中
- [ ] 索引创建成功

---

#### Task 1.2: 文件存储目录重构
**优先级**: P0  
**依赖**: Task 1.1

**工作内容**:
1. 修改 `FileStorageProperties` 配置类
2. 修改 `FileStorageService` 中的路径生成逻辑
3. 新增 `getUserDirectory(Long userId)` 方法
4. 更新文件上传时的路径拼接规则

**代码示例**:
```java
public Path getUserDirectory(Long userId) {
    Path userDir = rootLocation.resolve("user_" + userId).normalize();
    if (!Files.exists(userDir)) {
        Files.createDirectories(userDir);
    }
    return userDir;
}

// 上传时使用
Path userDir = getUserDirectory(userId);
Path targetPath = userDir.resolve(uniqueFilename);
```

**验收标准**:
- [ ] 新用户目录自动创建
- [ ] 文件保存到正确的用户子目录
- [ ] 路径安全检查仍然有效

---

#### Task 1.3: 数据迁移脚本开发
**优先级**: P0  
**依赖**: Task 1.2

**工作内容**:
1. 创建 `DataMigrationService` 组件
2. 实现文件迁移逻辑：
   - 遍历所有 file_records
   - 移动物理文件到对应用户目录
   - 更新数据库中的 file_path 字段
3. 添加迁移完成标记（避免重复执行）
4. 编写回滚脚本

**迁移流程**:
```
旧路径: uploads/abc123-file.zip
新路径: uploads/user_1/abc123-file.zip
数据库更新: file_path = "user_1/abc123-file.zip"
```

**验收标准**:
- [ ] 迁移脚本执行成功
- [ ] 所有文件移动到正确位置
- [ ] 数据库路径字段已更新
- [ ] 迁移后应用正常运行

---

### Phase 2: 账号管理模块

#### Task 2.1: AccountController 开发
**优先级**: P0  
**依赖**: Phase 1 完成

**工作内容**:
1. 创建 `AccountController` 类
2. 实现三个接口：
   - `GET /account/profile` - 查看个人信息
   - `POST /account/change-password` - 修改密码
   - `POST /account/change-email` - 修改邮箱
3. 添加权限控制（需登录）

**接口设计**:
```java
@Controller
@RequestMapping("/account")
public class AccountController {
    
    @GetMapping("/profile")
    public String profile(Model model, Authentication auth) {
        // 获取当前用户信息
        // 计算文件统计
        return "account/profile";
    }
    
    @PostMapping("/change-password")
    public String changePassword(@RequestParam String oldPassword,
                                 @RequestParam String newPassword,
                                 Authentication auth) {
        // 验证旧密码
        // 更新新密码
        return "redirect:/account/profile?success=password_changed";
    }
}
```

**验收标准**:
- [ ] 路由映射正确
- [ ] 权限控制生效
- [ ] 异常处理完善

---

#### Task 2.2: 修改密码功能
**优先级**: P0  
**依赖**: Task 2.1

**工作内容**:
1. 在 `UserService` 中添加 `changePassword()` 方法
2. 验证逻辑：
   - 旧密码匹配检查
   - 新密码长度 ≥ 6
   - 两次输入一致
3. BCrypt 加密新密码
4. 更新数据库
5. （可选）强制退出登录

**业务逻辑**:
```java
@Transactional
public void changePassword(Long userId, String oldPassword, String newPassword) {
    User user = userRepository.findById(userId)
        .orElseThrow(() -> new RuntimeException("用户不存在"));
    
    // 验证旧密码
    if (!passwordEncoder.matches(oldPassword, user.getPassword())) {
        throw new IllegalArgumentException("当前密码错误");
    }
    
    // 验证新密码
    if (newPassword.length() < 6) {
        throw new IllegalArgumentException("密码长度至少6位");
    }
    
    // 更新密码
    user.setPassword(passwordEncoder.encode(newPassword));
    userRepository.save(user);
}
```

**验收标准**:
- [ ] 旧密码验证正确
- [ ] 新密码加密存储
- [ ] 错误提示友好
- [ ] 事务回滚正常

---

#### Task 2.3: 个人信息页面开发
**优先级**: P1  
**依赖**: Task 2.1

**工作内容**:
1. 创建 `templates/account/profile.html`
2. 页面布局：
   - 基本信息展示区（用户名、角色、状态、创建时间）
   - 邮箱修改表单
   - 密码修改表单
   - 文件统计信息
3. 使用 Thymeleaf 绑定数据
4. 添加 CSRF token
5. 响应式设计

**页面结构**:
```html
<div class="profile-container">
    <!-- 基本信息 -->
    <div class="info-card">
        <h3>基本信息</h3>
        <p>用户名: <span th:text="${user.username}"></span></p>
        <p>邮  箱: <span th:text="${user.email}"></span></p>
    </div>
    
    <!-- 修改邮箱 -->
    <form th:action="@{/account/change-email}" method="post">
        <input type="email" name="email" th:value="${user.email}">
        <button type="submit">保存</button>
    </form>
    
    <!-- 修改密码 -->
    <form th:action="@{/account/change-password}" method="post">
        <input type="password" name="oldPassword" placeholder="当前密码">
        <input type="password" name="newPassword" placeholder="新密码">
        <input type="password" name="confirmPassword" placeholder="确认密码">
        <button type="submit">修改密码</button>
    </form>
</div>
```

**验收标准**:
- [ ] 页面布局美观
- [ ] 数据绑定正确
- [ ] 表单提交正常
- [ ] 移动端适配良好

---

### Phase 3: 多用户管理模块

#### Task 3.1: AdminUserController 开发
**优先级**: P0  
**依赖**: Phase 1 完成

**工作内容**:
1. 创建 `AdminUserController` 类
2. 添加 `@PreAuthorize("hasRole('ADMIN')")` 或手动权限检查
3. 实现五个接口：
   - `GET /admin/users` - 用户列表
   - `POST /admin/users` - 创建用户
   - `DELETE /admin/users/{id}` - 删除用户
   - `POST /admin/users/{id}/toggle-enabled` - 切换状态
   - `GET /admin/users/{id}` - 用户详情

**权限控制**:
```java
@Controller
@RequestMapping("/admin")
public class AdminUserController {
    
    @GetMapping("/users")
    public String listUsers(Model model, Authentication auth) {
        // 检查是否为管理员
        if (!isAdmin(auth)) {
            return "redirect:/files?error=access_denied";
        }
        
        // 查询用户列表
        return "admin/users";
    }
}
```

**验收标准**:
- [ ] 仅管理员可访问
- [ ] 普通用户访问返回 403
- [ ] 路由前缀统一为 /admin

---

#### Task 3.2: 用户列表查询（分页）
**优先级**: P0  
**依赖**: Task 3.1

**工作内容**:
1. 在 `UserRepository` 中添加分页查询方法
2. 支持关键词搜索、角色过滤、状态过滤
3. Controller 接收分页参数
4. 传递 Page<User> 到前端

**Repository 方法**:
```java
@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    
    @Query("SELECT u FROM User u WHERE " +
           "(:keyword IS NULL OR u.username LIKE %:keyword%) AND " +
           "(:role IS NULL OR u.role = :role) AND " +
           "(:enabled IS NULL OR u.enabled = :enabled)")
    Page<User> findByFilters(@Param("keyword") String keyword,
                             @Param("role") String role,
                             @Param("enabled") Boolean enabled,
                             Pageable pageable);
}
```

**验收标准**:
- [ ] 分页功能正常
- [ ] 搜索过滤生效
- [ ] 性能良好（1000+ 用户）

---

#### Task 3.3: 创建用户功能
**优先级**: P0  
**依赖**: Task 3.2

**工作内容**:
1. 实现用户创建逻辑
2. 验证用户名唯一性
3. 验证用户名格式（正则）
4. BCrypt 加密密码
5. 自动创建用户文件目录
6. 发送欢迎邮件（可选）

**业务逻辑**:
```java
@Transactional
public User createUser(CreateUserRequest request) {
    // 验证用户名唯一性
    if (userRepository.existsByUsername(request.getUsername())) {
        throw new IllegalArgumentException("用户名已存在");
    }
    
    // 验证用户名格式
    if (!request.getUsername().matches("^[a-zA-Z0-9_]{3,20}$")) {
        throw new IllegalArgumentException("用户名格式不正确");
    }
    
    // 创建用户
    User user = new User();
    user.setUsername(request.getUsername());
    user.setPassword(passwordEncoder.encode(request.getPassword()));
    user.setEmail(request.getEmail());
    user.setRole(request.getRole());
    user.setEnabled(request.getEnabled());
    user.setCreatedAt(LocalDateTime.now());
    
    User savedUser = userRepository.save(user);
    
    // 创建用户目录
    Path userDir = Paths.get("uploads/user_" + savedUser.getId());
    Files.createDirectories(userDir);
    
    return savedUser;
}
```

**验收标准**:
- [ ] 用户名唯一性检查
- [ ] 密码加密存储
- [ ] 用户目录自动创建
- [ ] 错误提示清晰

---

#### Task 3.4: 删除用户功能
**优先级**: P0  
**依赖**: Task 3.3

**工作内容**:
1. 实现用户删除逻辑
2. 检查不能删除自己
3. 删除用户所有文件（物理删除）
4. 删除用户目录
5. 删除数据库记录（级联删除文件记录）
6. 添加二次确认机制（前端）

**业务逻辑**:
```java
@Transactional
public void deleteUser(Long targetUserId, Long currentUserId) {
    // 不能删除自己
    if (targetUserId.equals(currentUserId)) {
        throw new IllegalArgumentException("不能删除自己的账户");
    }
    
    User user = userRepository.findById(targetUserId)
        .orElseThrow(() -> new RuntimeException("用户不存在"));
    
    // 删除用户所有文件
    List<FileRecord> files = fileRecordRepository.findByUserId(targetUserId);
    for (FileRecord file : files) {
        try {
            Files.deleteIfExists(Paths.get(file.getFilePath()));
        } catch (IOException e) {
            log.error("删除文件失败: {}", file.getFilename(), e);
        }
    }
    
    // 删除文件记录
    fileRecordRepository.deleteByUserId(targetUserId);
    
    // 删除用户目录
    Path userDir = Paths.get("uploads/user_" + targetUserId);
    deleteDirectory(userDir);
    
    // 删除用户
    userRepository.delete(user);
}

private void deleteDirectory(Path directory) throws IOException {
    if (Files.exists(directory)) {
        Files.walk(directory)
            .sorted(Comparator.reverseOrder())
            .map(Path::toFile)
            .forEach(File::delete);
    }
}
```

**验收标准**:
- [ ] 不能删除自己
- [ ] 物理文件全部删除
- [ ] 用户目录被移除
- [ ] 数据库记录清理完毕

---

#### Task 3.5: 禁用/启用用户功能
**优先级**: P0  
**依赖**: Task 3.3

**工作内容**:
1. 切换用户 `enabled` 字段
2. 被禁用用户登录时拒绝
3. （可选）使当前 Session 失效

**CustomUserDetailsService 修改**:
```java
@Override
public UserDetails loadUserByUsername(String username) {
    User user = userRepository.findByUsername(username)
        .orElseThrow(() -> new UsernameNotFoundException("用户不存在"));
    
    if (!user.isEnabled()) {
        throw new DisabledException("账户已被禁用，请联系管理员");
    }
    
    return new CustomUserDetails(user);
}
```

**验收标准**:
- [ ] 状态切换成功
- [ ] 被禁用用户无法登录
- [ ] 错误提示友好

---

#### Task 3.6: 用户管理页面开发
**优先级**: P1  
**依赖**: Task 3.1-3.5

**工作内容**:
1. 创建 `templates/admin/users.html`
2. 用户列表表格展示
3. 新建用户模态框
4. 操作按钮（编辑、删除、禁用）
5. 分页控件
6. 搜索框

**页面功能**:
- 表格列：ID、用户名、邮箱、角色、状态、创建时间、操作
- 操作按钮：编辑（弹窗）、删除（确认）、禁用/启用（切换）
- 顶部按钮：新建用户、导出 Excel（可选）

**验收标准**:
- [ ] 列表展示完整
- [ ] 操作按钮功能正常
- [ ] 分页流畅
- [ ] 响应式布局

---

### Phase 4: 文件管理增强

#### Task 4.1: AdminFileController 开发
**优先级**: P0  
**依赖**: Phase 3 完成

**工作内容**:
1. 创建 `AdminFileController` 类
2. 实现全局文件管理接口
3. 添加管理员权限检查

**接口清单**:
```java
@Controller
@RequestMapping("/admin/files")
public class AdminFileController {
    
    @GetMapping
    public String listFiles(@RequestParam(required = false) Long userId,
                           Model model) {
        // 查询所有文件或指定用户文件
        return "admin/files";
    }
    
    @PostMapping("/{id}/delete")
    public String deleteFile(@PathVariable Long id) {
        // 管理员删除文件
        return "redirect:/admin/files";
    }
    
    @GetMapping("/{id}/download")
    public ResponseEntity<Resource> downloadFile(@PathVariable Long id) {
        // 管理员下载文件
    }
}
```

**验收标准**:
- [ ] 路由配置正确
- [ ] 权限控制生效
- [ ] 异常处理完善

---

#### Task 4.2: 全局文件列表查询
**优先级**: P0  
**依赖**: Task 4.1

**工作内容**:
1. 在 `FileRecordRepository` 中添加查询方法
2. 支持按用户筛选
3. 支持分页
4. 关联查询用户信息

**Repository 方法**:
```java
@Repository
public interface FileRecordRepository extends JpaRepository<FileRecord, Long> {
    
    @Query("SELECT f FROM FileRecord f JOIN FETCH f.user u WHERE " +
           "(:userId IS NULL OR f.userId = :userId) AND " +
           "(:keyword IS NULL OR f.originalFilename LIKE %:keyword%)")
    Page<FileRecord> findByFilters(@Param("userId") Long userId,
                                   @Param("keyword") String keyword,
                                   Pageable pageable);
}
```

**验收标准**:
- [ ] 可以查询所有文件
- [ ] 可以按用户筛选
- [ ] 分页功能正常
- [ ] 性能良好

---

#### Task 4.3: 文件管理页面开发
**优先级**: P1  
**依赖**: Task 4.2

**工作内容**:
1. 创建 `templates/admin/files.html`
2. 用户筛选下拉框
3. 文件列表表格
4. 搜索框
5. 操作按钮（下载、删除）

**页面布局**:
```
┌─────────────────────────────────────┐
│ 用户: [全部 ▼]  🔍 [搜索...]       │
├─────────────────────────────────────┤
│ 文件名 | 用户 | 大小 | 时间 | 操作 │
├─────────────────────────────────────┤
│ ...                                 │
└─────────────────────────────────────┘
```

**验收标准**:
- [ ] 筛选功能正常
- [ ] 搜索功能正常
- [ ] 操作按钮可用
- [ ] 分页流畅

---

### Phase 5: 文件类型扩展

#### Task 5.1: 文件类型配置扩展
**优先级**: P0  
**依赖**: Phase 1 完成

**工作内容**:
1. 在 `application.properties` 中添加配置
2. 扩展 `FileStorageProperties` 类
3. 添加白名单列表

**配置示例**:
```properties
file.allowed-extensions=.jpg,.jpeg,.png,.gif,.webp,.zip,.pdf,.doc,.docx,.txt,.csv
file.allowed-types=image/jpeg,image/png,image/gif,image/webp,application/zip,application/pdf
file.max-file-size=10485760  # 10MB
```

**验收标准**:
- [ ] 配置项读取正常
- [ ] 默认值合理
- [ ] 可随时修改

---

#### Task 5.2: 文件类型验证逻辑
**优先级**: P0  
**依赖**: Task 5.1

**工作内容**:
1. 在 `FileStorageService` 中添加 `validateFileType()` 方法
2. 检查文件扩展名
3. （可选）检查 MIME 类型
4. 检查文件大小
5. 抛出友好的异常消息

**验证逻辑**:
```java
private void validateFileType(MultipartFile file) {
    String filename = file.getOriginalFilename();
    String extension = getFileExtension(filename).toLowerCase();
    
    // 检查扩展名
    if (!fileStorageProperties.getAllowedExtensions().contains(extension)) {
        throw new IllegalArgumentException("不支持的文件类型: " + extension);
    }
    
    // 检查文件大小
    if (file.getSize() > fileStorageProperties.getMaxFileSize()) {
        throw new IllegalArgumentException("文件大小超过限制（最大10MB）");
    }
}

private String getFileExtension(String filename) {
    int lastDotIndex = filename.lastIndexOf(".");
    return lastDotIndex == -1 ? "" : filename.substring(lastDotIndex);
}
```

**验收标准**:
- [ ] 允许的类型可以上传
- [ ] 禁止的类型被拦截
- [ ] 错误提示清晰
- [ ] 大小限制生效

---

#### Task 5.3: 前端文件类型图标展示
**优先级**: P2  
**依赖**: Task 5.2

**工作内容**:
1. 根据文件扩展名分配图标
2. 添加 CSS 样式
3. 在文件列表中显示图标

**图标映射**:
```javascript
const fileIcons = {
    '.jpg': '📷', '.jpeg': '📷', '.png': '📷', '.gif': '📷',
    '.pdf': '📄', '.doc': '📄', '.docx': '📄',
    '.zip': '📦', '.rar': '📦', '.7z': '📦',
    '.txt': '📝', '.csv': '📝'
};
```

**验收标准**:
- [ ] 不同类型显示不同图标
- [ ] 图标美观清晰
- [ ] 未知类型有默认图标

---

### Phase 6: 测试与优化

#### Task 6.1: 单元测试补充
**优先级**: P0  
**依赖**: 所有功能开发完成

**工作内容**:
1. 为 Service 层编写单元测试
2. Mock Repository 依赖
3. 测试边界条件
4. 目标覆盖率 ≥ 80%

**测试用例示例**:
```java
@Test
void testChangePassword_Success() {
    // Given
    when(userRepository.findById(1L)).thenReturn(Optional.of(user));
    when(passwordEncoder.matches("oldPass", user.getPassword())).thenReturn(true);
    
    // When
    userService.changePassword(1L, "oldPass", "newPass123");
    
    // Then
    verify(userRepository).save(any(User.class));
    verify(passwordEncoder).encode("newPass123");
}
```

**验收标准**:
- [ ] 覆盖率 ≥ 80%
- [ ] 所有测试通过
- [ ] 无警告信息

---

#### Task 6.2: 安全测试
**优先级**: P0  
**依赖**: Task 6.1

**工作内容**:
1. 权限绕过测试
2. SQL 注入测试
3. XSS 攻击测试
4. CSRF 攻击测试
5. 路径遍历测试
6. 文件上传漏洞测试

**测试场景**:
| 场景 | 预期结果 |
|------|---------|
| 普通用户访问 /admin/* | 403 Forbidden |
| 上传 .exe 文件 | 被拦截 |
| 路径遍历 ../../../etc/passwd | 被拦截 |
| 不带 CSRF token 提交 | 403 Forbidden |
| 禁用用户登录 | 拒绝登录 |

**验收标准**:
- [ ] 无高危安全漏洞
- [ ] 所有攻击场景被防护
- [ ] 安全日志记录完整

---

#### Task 6.3: 性能测试
**优先级**: P1  
**依赖**: Task 6.2

**工作内容**:
1. 使用 JMeter 进行压力测试
2. 测试场景：
   - 并发上传文件（50 用户）
   - 并发下载文件（100 用户）
   - 大量文件列表查询（10000+ 记录）
3. 监控指标：
   - 响应时间
   - 吞吐量
   - CPU/内存占用
   - 数据库连接池

**性能目标**:
- 页面加载时间 < 2 秒
- API 响应时间 < 500ms
- 支持 50+ 并发用户
- 内存占用 < 512MB

**验收标准**:
- [ ] 达到性能目标
- [ ] 无内存泄漏
- [ ] 数据库查询优化

---

## 🚀 部署计划

### 部署阶段 1: 测试环境（第 5 周末）
**目标**: 在测试环境验证所有功能

**步骤**:
1. 备份生产数据库
2. 部署新版本到测试服务器
3. 执行数据迁移脚本
4. 运行自动化测试套件
5. 手动功能测试
6. 收集反馈并修复 Bug

**验收**:
- [ ] 所有功能测试通过
- [ ] 性能满足要求
- [ ] 无严重 Bug

---

### 部署阶段 2: 生产环境（第 6 周末）
**目标**: 正式切换到新版本

**步骤**:
1. **准备阶段**（提前 1 天）
   - 通知用户维护时间窗口
   - 备份数据库和文件
   - 准备回滚方案

2. **维护窗口**（预计 2 小时）
   - 停止应用服务
   - 备份当前版本
   - 部署新版本
   - 执行数据迁移
   - 启动应用
   - 验证核心功能

3. **监控阶段**（部署后 24 小时）
   - 监控系统资源
   - 查看错误日志
   - 收集用户反馈
   - 快速响应问题

**回滚方案**:
如果部署失败，立即回滚：
```bash
# 1. 停止新版本
systemctl stop fileservice

# 2. 恢复数据库
mysql -u root -p fileservice < backup_before_migration.sql

# 3. 恢复文件
tar -xzf uploads_backup.tar.gz

# 4. 启动旧版本
systemctl start fileservice-old
```

**验收**:
- [ ] 生产环境运行稳定
- [ ] 用户反馈良好
- [ ] 监控指标正常

---

## 📊 风险管理

### 技术风险

| 风险 | 概率 | 影响 | 应对措施 |
|------|------|------|---------|
| 数据迁移失败 | 低 | 高 | 完整备份 + 多次测试迁移 |
| 性能下降 | 中 | 中 | 性能测试 + 索引优化 |
| 安全漏洞 | 低 | 高 | 安全审计 + 渗透测试 |
| 兼容性问题 | 低 | 中 | 充分测试 + 灰度发布 |

### 业务风险

| 风险 | 应对措施 |
|------|---------|
| 用户不适应新功能 | 提供用户手册 + 培训视频 |
| 管理员误操作 | 二次确认 + 操作日志 |
| 磁盘空间不足 | 监控告警 + 自动清理策略 |

---

## 📈 成功指标

### 功能指标
- [ ] 所有计划功能 100% 实现
- [ ] 单元测试覆盖率 ≥ 80%
- [ ] 集成测试通过率 100%

### 性能指标
- [ ] 页面加载时间 < 2 秒
- [ ] API 响应时间 < 500ms
- [ ] 支持 50+ 并发用户

### 质量指标
- [ ] 生产环境 Bug 数 < 5 个/月
- [ ] 用户满意度 ≥ 90%
- [ ] 系统可用性 ≥ 99.5%

---

## 📚 交付物清单

### 代码交付
- [ ] 完整的源代码（Git 仓库）
- [ ] 数据库迁移脚本
- [ ] 配置文件模板

### 文档交付
- [ ] 功能扩展设计文档（本文档）
- [ ] API 接口文档
- [ ] 用户操作手册
- [ ] 部署运维手册
- [ ] 测试报告

### 培训交付
- [ ] 管理员培训材料
- [ ] 用户使用指南
- [ ] 常见问题 FAQ

---

## 🎓 团队协作

### 每日站会
- **时间**: 每天上午 10:00
- **时长**: 15 分钟
- **内容**:
  - 昨天完成了什么
  - 今天计划做什么
  - 遇到什么阻碍

### 代码审查
- **工具**: GitHub/GitLab Pull Request
- **要求**:
  - 每个 PR 至少 1 人审查
  - 审查通过后才能合并
  - 关注代码质量、安全性、性能

### 周报
- **时间**: 每周五下午
- **内容**:
  - 本周进度总结
  - 下周工作计划
  - 风险和问题

---

## 📞 联系方式

**项目负责人**: [填写姓名]  
**技术支持**: [填写邮箱]  
**紧急联系**: [填写电话]

---

**文档版本**: 1.0  
**创建日期**: 2026-06-11  
**最后更新**: 2026-06-11
