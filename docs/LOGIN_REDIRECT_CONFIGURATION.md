# 管理员登录默认进入控制台配置

## 📋 任务概述

实现管理员登录后自动跳转到控制台页面（`/admin/dashboard`），普通用户登录后跳转到文件列表页面（`/files`）。

---

## ✅ 完成的工作

### 1. 创建自定义认证成功处理器

#### CustomAuthenticationSuccessHandler.java
**文件路径**: [CustomAuthenticationSuccessHandler.java](file://D:\ideaProject\fileService\src\main\java\net\docn\fileservice\config\CustomAuthenticationSuccessHandler.java)

**核心功能**：

```java
@Component
public class CustomAuthenticationSuccessHandler extends SavedRequestAwareAuthenticationSuccessHandler {

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request,
                                        HttpServletResponse response,
                                        Authentication authentication) throws IOException, ServletException {
        
        // 获取用户详情
        Object principal = authentication.getPrincipal();
        
        if (principal instanceof CustomUserDetails) {
            CustomUserDetails userDetails = (CustomUserDetails) principal;
            
            // 根据角色决定跳转路径
            if (userDetails.isAdmin()) {
                // 管理员跳转到控制台
                getRedirectStrategy().sendRedirect(request, response, "/admin/dashboard");
            } else {
                // 普通用户跳转到文件列表
                super.onAuthenticationSuccess(request, response, authentication);
            }
        } else {
            // 其他情况使用默认行为
            super.onAuthenticationSuccess(request, response, authentication);
        }
    }
}
```

**关键逻辑**：
- 继承 `SavedRequestAwareAuthenticationSuccessHandler` 保留原有功能
- 检查用户的角色（通过 `isAdmin()` 方法）
- 管理员 → `/admin/dashboard`
- 普通用户 → `/files`（父类默认行为）

---

### 2. 修改 SecurityConfig

**文件路径**: [SecurityConfig.java](file://D:\ideaProject\fileService\src\main\java\net\docn\fileservice\config\SecurityConfig.java)

**主要变更**：

#### 注入自定义处理器

```java
@Autowired
private CustomAuthenticationSuccessHandler customAuthenticationSuccessHandler;
```

#### 替换默认成功处理器

```java
// 修改前
.formLogin(form -> form
    .loginPage("/login")
    .defaultSuccessUrl("/files", true)
    .permitAll()
)

// 修改后
.formLogin(form -> form
    .loginPage("/login")
    .successHandler(customAuthenticationSuccessHandler) // 使用自定义认证成功处理器
    .permitAll()
)
```

---

## 🔄 工作流程

```mermaid
graph LR
    A[用户登录] --> B[验证码验证]
    B --> C[用户名密码验证]
    C --> D{认证成功?}
    D -->|是| E[CustomAuthenticationSuccessHandler]
    D -->|否| F[登录失败]
    E --> G{判断角色}
    G -->|ADMIN| H[/admin/dashboard]
    G -->|USER| I[/files]
```

---

## 🎯 技术实现细节

### 1. 角色判断

使用 `CustomUserDetails.isAdmin()` 方法判断用户角色：

```java
public boolean isAdmin() {
    return "ADMIN".equals(user.getRole());
}
```

### 2. 重定向策略

- **管理员**: 使用 `getRedirectStrategy().sendRedirect()` 直接重定向到控制台
- **普通用户**: 调用 `super.onAuthenticationSuccess()` 使用 Spring Security 的默认行为

### 3. SavedRequestAwareAuthenticationSuccessHandler

这个父类提供了智能的重定向功能：
- 如果用户访问了受保护的资源被拦截到登录页，登录成功后会返回原资源
- 如果是直接访问登录页，则跳转到默认 URL

---

## 📊 登录跳转规则

| 用户角色 | 登录前访问 | 登录后跳转 | 说明 |
|---------|-----------|-----------|------|
| ADMIN | 直接访问 /login | /admin/dashboard | 管理员控制台 |
| ADMIN | 访问 /admin/users 被拦截 | /admin/dashboard | 始终跳转到控制台 |
| USER | 直接访问 /login | /files | 文件列表 |
| USER | 访问 /files 被拦截 | /files | 返回原请求 |

---

## 🚀 使用方法

### 1. 重启应用

停止当前运行的应用，然后重新启动。

### 2. 测试管理员登录

1. 访问：`http://localhost:8067/login`
2. 使用管理员账号登录（用户名：`admin`，密码：`admin123`）
3. 预期结果：自动跳转到 `http://localhost:8067/admin/dashboard`

### 3. 测试普通用户登录

1. 访问：`http://localhost:8067/login`
2. 使用普通用户账号登录
3. 预期结果：自动跳转到 `http://localhost:8067/files`

---

## 💡 扩展建议

### 1. 添加登录日志

可以在 `CustomAuthenticationSuccessHandler` 中添加日志记录：

```java
log.info("用户 {} 登录成功，角色: {}, 跳转到: {}", 
    userDetails.getUsername(), 
    userDetails.getRole(),
    userDetails.isAdmin() ? "/admin/dashboard" : "/files");
```

### 2. 添加会话信息

可以将会话信息存储到数据库：

```java
// 记录登录时间、IP 地址等
user.setLastLoginTime(LocalDateTime.now());
user.setLastLoginIp(getClientIp(request));
userRepository.save(user);
```

### 3. 支持更多角色

如果有更多角色（如 OPERATOR、VIEWER），可以扩展跳转逻辑：

```java
switch (userDetails.getRole()) {
    case "ADMIN":
        targetUrl = "/admin/dashboard";
        break;
    case "OPERATOR":
        targetUrl = "/operator/panel";
        break;
    default:
        targetUrl = "/files";
}
```

---

## 🔍 调试技巧

### 1. 查看认证信息

在处理器中添加调试代码：

```java
log.debug("认证对象: {}", authentication.getClass().getName());
log.debug("Principal: {}", principal.getClass().getName());
log.debug("用户角色: {}", userDetails.getRole());
```

### 2. 检查重定向

使用浏览器开发者工具查看 Network 标签：
- 应该看到 POST /login 请求
- 响应状态码应该是 302 Found
- Location 头应该是 `/admin/dashboard` 或 `/files`

---

## ⚠️ 注意事项

1. **权限控制**: 确保 `/admin/**` 路径仍然需要 `ROLE_ADMIN` 权限
2. **缓存问题**: 修改后可能需要清除浏览器缓存和 IDE 缓存
3. **安全性**: 不要在 URL 中传递敏感信息
4. **兼容性**: 确保所有用户都有正确的角色设置

---

## 📝 相关文件

- [CustomAuthenticationSuccessHandler.java](file://D:\ideaProject\fileService\src\main\java\net\docn\fileservice\config\CustomAuthenticationSuccessHandler.java) - 自定义认证成功处理器
- [SecurityConfig.java](file://D:\ideaProject\fileService\src\main\java\net\docn\fileservice\config\SecurityConfig.java) - Spring Security 配置
- [CustomUserDetails.java](file://D:\ideaProject\fileService\src\main\java\net\docn\fileservice\service\CustomUserDetails.java) - 自定义用户详情类
- [AdminDashboardController.java](file://D:\ideaProject\fileService\src\main\java\net\docn\fileservice\controller\AdminDashboardController.java) - 管理员控制台控制器

---

## 🎉 总结

现在系统已经实现了基于角色的登录跳转：

1. ✅ 创建了自定义认证成功处理器
2. ✅ 修改了 SecurityConfig 使用自定义处理器
3. ✅ 管理员登录后自动跳转到控制台
4. ✅ 普通用户登录后跳转到文件列表
5. ✅ 保留了原有的智能重定向功能

重启应用后即可体验新的登录跳转逻辑！🚀
