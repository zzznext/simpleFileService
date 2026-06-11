# 非法文件访问错误修复

## 🐛 问题描述

### 错误信息
```
java.lang.RuntimeException: 非法的文件访问
	at net.docn.fileservice.service.FileStorageService.downloadFile(FileStorageService.java:187)
```

### 触发场景
用户尝试下载文件时，URL 为：
```
http://localhost:8067/download/user_1/0ee6718a-3a8b-41c9-9452-bbc69b0cde1e_docn.net_nginx.zip
```

---

## 🔍 根本原因

### 1. 路径验证逻辑错误

**原始代码**（FileStorageService.java 第 185 行）：
```java
Path filePath = rootLocation.resolve(filename).normalize();

// ❌ 错误的比较方式
if (!filePath.startsWith(rootLocation)) {
    throw new RuntimeException("非法的文件访问");
}
```

### 2. 问题分析

| 变量 | 类型 | 值示例 |
|------|------|--------|
| `rootLocation` | `Path` (相对) | `./uploads` |
| `filename` | `String` | `user_1/0ee6718a-...zip` |
| `filePath` | `Path` (绝对 + normalize) | `D:\ideaProject\fileService\uploads\user_1\0ee6718a-...zip` |

**问题**：
- `rootLocation` 是相对路径（`./uploads`）
- `filePath` 是绝对路径（`D:\...\uploads\user_1\...`）
- `filePath.startsWith(rootLocation)` 比较失败，因为一个是绝对路径，一个是相对路径

### 3. 可能的额外问题

- URL 中可能包含前导斜杠 `/user_1/...`
- Spring 的 `@PathVariable` 可能传递带斜杠的路径

---

## ✅ 修复方案

### 修改后的代码

```java
public Path downloadFile(String filename) {
    log.info("尝试下载文件: {}", filename);
    log.info("rootLocation: {}", rootLocation);
    
    // 1. 清理路径：移除开头的斜杠
    String cleanFilename = filename;
    if (cleanFilename.startsWith("/") || cleanFilename.startsWith("\\")) {
        cleanFilename = cleanFilename.substring(1);
    }
    
    log.info("清理后的文件名: {}", cleanFilename);
    
    // 2. 防止目录遍历攻击
    Path filePath = rootLocation.resolve(cleanFilename).normalize();
    log.info("解析后的完整路径: {}", filePath);
    
    // 3. 统一转换为绝对路径进行比较
    Path absoluteRootLocation = rootLocation.toAbsolutePath().normalize();
    if (!filePath.toAbsolutePath().normalize().startsWith(absoluteRootLocation)) {
        log.error("非法的文件访问尝试: {}", filename);
        log.error("解析路径: {}", filePath);
        log.error("根目录: {}", absoluteRootLocation);
        throw new RuntimeException("非法的文件访问");
    }
    
    if (!Files.exists(filePath)) {
        log.error("文件不存在: {}", filePath);
        throw new RuntimeException("文件不存在: " + filename);
    }
    
    log.info("文件存在，准备下载");
    return filePath;
}
```

### 关键改进

#### 1. 路径清理
```java
if (cleanFilename.startsWith("/") || cleanFilename.startsWith("\\")) {
    cleanFilename = cleanFilename.substring(1);
}
```
- 移除前导斜杠，避免路径解析问题

#### 2. 统一路径格式
```java
Path absoluteRootLocation = rootLocation.toAbsolutePath().normalize();
if (!filePath.toAbsolutePath().normalize().startsWith(absoluteRootLocation)) {
    // ...
}
```
- 将两个路径都转换为**绝对路径 + normalize**
- 确保比较的是相同格式的路径

#### 3. 增强日志
```java
log.info("清理后的文件名: {}", cleanFilename);
log.info("解析后的完整路径: {}", filePath);
log.error("解析路径: {}", filePath);
log.error("根目录: {}", absoluteRootLocation);
```
- 记录详细的路径信息，便于调试

---

## 🧪 测试验证

### 测试步骤

1. **重启应用**
   ```
   - 停止当前运行的应用
   - 重新启动
   ```

2. **访问下载链接**
   ```
   http://localhost:8067/download/user_1/0ee6718a-3a8b-41c9-9452-bbc69b0cde1e_docn.net_nginx.zip
   ```

3. **检查控制台日志**

   **预期输出**：
   ```
   INFO  --- 下载请求: user_1/0ee6718a-3a8b-41c9-9452-bbc69b0cde1e_docn.net_nginx.zip
   INFO  --- 尝试下载文件: user_1/0ee6718a-3a8b-41c9-9452-bbc69b0cde1e_docn.net_nginx.zip
   INFO  --- rootLocation: ./uploads
   INFO  --- 清理后的文件名: user_1/0ee6718a-3a8b-41c9-9452-bbc69b0cde1e_docn.net_nginx.zip
   INFO  --- 解析后的完整路径: D:\ideaProject\fileService\uploads\user_1\0ee6718a-3a8b-41c9-9452-bbc69b0cde1e_docn.net_nginx.zip
   INFO  --- 文件存在，准备下载
   ```

4. **验证文件下载**
   - 浏览器应该开始下载文件
   - 文件名正确（去掉 UUID 前缀）
   - Content-Type 正确（ZIP 文件应为 `application/zip`）

---

## 📊 修复效果对比

| 场景 | 修复前 | 修复后 |
|------|--------|--------|
| 路径比较 | ❌ 相对路径 vs 绝对路径 → 失败 | ✅ 绝对路径 vs 绝对路径 → 成功 |
| 前导斜杠处理 | ❌ 可能导致路径错误 | ✅ 自动清理 |
| 日志输出 | ⚠️ 信息不足 | ✅ 详细的调试信息 |
| 安全性 | ⚠️ 路径验证不完整 | ✅ 严格的路径遍历防护 |

---

## 🔒 安全防护说明

### 路径遍历攻击防护

修复后的代码仍然保持完整的安全防护：

1. **路径清理**：移除前导斜杠
2. **路径规范化**：使用 `normalize()` 消除 `..` 和 `.` 
3. **白名单验证**：确保最终路径在 `uploads/` 目录内
4. **文件存在性检查**：防止返回不存在的文件

### 攻击示例

```
恶意请求：
http://localhost:8067/download/user_1/../../../etc/passwd

防御过程：
1. 清理路径 → user_1/../../../etc/passwd
2. resolve → uploads/user_1/../../../etc/passwd
3. normalize → /etc/passwd
4. startsWith 检查 → ❌ 不在 uploads/ 目录内 → 拒绝访问
```

---

## 📝 相关文件

- [FileStorageService.java](file://D:\ideaProject\fileService\src\main\java\net\docn\fileservice\service\FileStorageService.java#L173-L208) - 下载方法修复
- [FileController.java](file://D:\ideaProject\fileService\src\main\java\net\docn\fileservice\controller\FileController.java#L118-L157) - Controller 层调用
- [SecurityConfig.java](file://D:\ideaProject\fileService\src\main\java\net\docn\fileservice\config\SecurityConfig.java#L30) - `/download/**` 已配置 permitAll()

---

## 💡 经验总结

### Java Path 比较的最佳实践

1. **始终使用相同格式的路径进行比较**
   ```java
   // ❌ 错误
   path1.startsWith(path2)  // 如果一个是绝对路径，一个是相对路径，会失败
   
   // ✅ 正确
   path1.toAbsolutePath().normalize().startsWith(
       path2.toAbsolutePath().normalize()
   )
   ```

2. **使用 `normalize()` 消除路径中的特殊字符**
   - `..` （父目录）
   - `.` （当前目录）
   - 多余的分隔符

3. **记录详细的路径日志**
   - 原始输入
   - 清理后的值
   - 解析后的完整路径
   - 根目录路径

### Spring MVC 路径变量注意事项

- `{*pathVariable}` 可以匹配包含 `/` 的路径
- 但可能会包含前导斜杠
- 需要手动清理和处理

---

## ✅ 验收标准

- [x] 代码已修复，无编译错误
- [ ] 应用已重启
- [ ] 下载链接可以正常访问
- [ ] 文件可以成功下载
- [ ] 日志输出清晰完整
- [ ] 路径遍历攻击被正确阻止
