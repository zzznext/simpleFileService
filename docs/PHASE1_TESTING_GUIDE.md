# Phase 1 功能测试指南

## 🎯 测试目标

验证 Phase 1 基础架构改造的所有功能是否正常工作。

---

## 📋 测试清单

### 1. 数据库 Schema 验证 ✅

#### 1.1 检查 users 表新字段

```sql
USE fileservice;

-- 查看 users 表结构
DESCRIBE users;
```

**预期结果**:
```
+---------------+--------------+------+-----+-------------------+
| Field         | Type         | Null | Key | Default           |
+---------------+--------------+------+-----+-------------------+
| id            | bigint       | NO   | PRI | NULL              |
| username      | varchar(50)  | NO   | UNI | NULL              |
| password      | varchar(255) | NO   |     | NULL              |
| email         | varchar(100) | YES  |     | NULL              |
| role          | varchar(20)  | NO   | MUL | USER              | ← 新增
| enabled       | bit(1)       | NO   | MUL | 1                 | ← 新增
| last_login_at | timestamp    | YES  |     | NULL              | ← 新增
| created_at    | timestamp    | NO   |     | CURRENT_TIMESTAMP |
| updated_at    | timestamp    | NO   |     | CURRENT_TIMESTAMP |
+---------------+--------------+------+-----+-------------------+
```

#### 1.2 检查索引

```sql
-- 查看 users 表索引
SHOW INDEX FROM users;

-- 查看 file_records 表索引
SHOW INDEX FROM file_records;
```

**预期新增索引**:
- `idx_users_role` (users.role)
- `idx_users_enabled` (users.enabled)
- `idx_file_records_filename` (file_records.original_filename)
- `idx_file_records_content_type` (file_records.content_type)

#### 1.3 验证默认管理员角色

```sql
SELECT username, role, enabled FROM users WHERE username = 'admin';
```

**预期结果**:
```
+----------+-------+---------+
| username | role  | enabled |
+----------+-------+---------+
| admin    | ADMIN | 1       |
+----------+-------+---------+
```

---

### 2. 配置加载验证

#### 2.1 启动应用

```bash
# 使用 IDE 运行 FileServiceApplication
# 或使用 Maven（如果已配置）
.\mvnw.cmd spring-boot:run
```

#### 2.2 检查启动日志

**预期日志**:
```
文件存储根目录初始化完成: D:\ideaProject\fileService\uploads
支持的文件类型: .jpg, .jpeg, .png, .gif, .webp, .zip, .pdf, .doc, .docx, .txt, .csv
```

如果看到这些日志，说明配置加载成功！

---

### 3. 用户目录自动创建测试

#### 3.1 登录系统

访问: http://localhost:8067/login  
用户名: `admin`  
密码: `admin123`

#### 3.2 上传一个测试文件

1. 进入文件管理页面: http://localhost:8067/files
2. 选择一个图片文件（如 test.jpg）或文档（如 test.pdf）
3. 点击上传

#### 3.3 验证目录创建

```bash
# 在 PowerShell 中执行
ls uploads/
```

**预期结果**:
```
Directory: D:\ideaProject\fileService\uploads

Mode                 LastWriteTime         Length Name
----                 -------------         ------ ----
d-----         2026/6/11     20:30                user_1
```

```bash
# 查看用户目录内容
ls uploads/user_1/
```

**预期结果**:
```
-a----         2026/6/11     20:30        123456 abc123-test.jpg
```

#### 3.4 验证数据库记录

```sql
SELECT id, filename, original_filename, file_path, user_id 
FROM file_records 
ORDER BY id DESC LIMIT 1;
```

**预期结果**:
```
+----+----------------------------------+--------------------+---------------------------+---------+
| id | filename                         | original_filename  | file_path                 | user_id |
+----+----------------------------------+--------------------+---------------------------+---------+
|  1 | abc123-test.jpg                  | test.jpg           | user_1/abc123-test.jpg    |       1 |
+----+----------------------------------+--------------------+---------------------------+---------+
```

**关键点**: `file_path` 应该是相对路径 `user_1/xxx`，而不是绝对路径！

---

### 4. 多文件类型上传测试

#### 4.1 测试允许的文件类型

| 文件类型 | 扩展名 | 测试结果 | 备注 |
|---------|--------|---------|------|
| JPEG 图片 | .jpg, .jpeg | ⏳ 待测试 | 应该成功 |
| PNG 图片 | .png | ⏳ 待测试 | 应该成功 |
| GIF 图片 | .gif | ⏳ 待测试 | 应该成功 |
| WebP 图片 | .webp | ⏳ 待测试 | 应该成功 |
| ZIP 压缩包 | .zip | ⏳ 待测试 | 应该成功 |
| PDF 文档 | .pdf | ⏳ 待测试 | 应该成功 |
| Word 文档 | .doc, .docx | ⏳ 待测试 | 应该成功 |
| 文本文件 | .txt, .csv | ⏳ 待测试 | 应该成功 |

#### 4.2 测试禁止的文件类型

尝试上传以下类型的文件，**应该被拒绝**：

| 文件类型 | 扩展名 | 预期结果 | 错误消息 |
|---------|--------|---------|---------|
| 可执行文件 | .exe | ❌ 拒绝 | "不支持的文件类型: .exe" |
| JavaScript | .js | ❌ 拒绝 | "不支持的文件类型: .js" |
| Python 脚本 | .py | ❌ 拒绝 | "不支持的文件类型: .py" |

#### 4.3 测试文件大小限制

**测试步骤**:
1. 准备一个大于 10MB 的文件
2. 尝试上传

**预期结果**:
```
错误消息: "文件大小超过限制（最大10.00 MB）"
```

---

### 5. 文件下载测试

#### 5.1 普通用户下载

1. 在文件列表中找到已上传的文件
2. 点击下载按钮

**验证**:
- ✅ 文件可以正常下载
- ✅ 文件名正确
- ✅ 文件内容完整

#### 5.2 公开下载链接

访问: `http://localhost:8067/download/user_1/abc123-test.jpg`

**验证**:
- ✅ 无需登录即可下载
- ✅ 文件正确

---

### 6. 文件删除测试

#### 6.1 删除个人文件

1. 在文件列表中选择一个文件
2. 点击删除按钮
3. 确认删除

**验证**:
```bash
# 检查物理文件是否删除
ls uploads/user_1/
```

```sql
-- 检查数据库记录是否删除
SELECT COUNT(*) FROM file_records WHERE filename = 'abc123-test.jpg';
-- 应该返回 0
```

---

### 7. 路径安全测试

#### 7.1 目录遍历攻击测试

尝试访问:
```
http://localhost:8067/download/../../../windows/system32/drivers/etc/hosts
```

**预期结果**: 
- ❌ 返回 404 或错误
- ✅ 不能访问系统文件

#### 7.2 跨用户文件访问测试

**场景**: 用户 ID=1 尝试访问用户 ID=2 的文件

**测试步骤**:
1. 创建两个用户（需要手动插入数据库）
2. 用户1上传文件
3. 尝试通过修改 URL 访问用户2的文件

**预期结果**:
- ✅ 无法访问其他用户的文件
- ✅ 返回权限错误或文件不存在

---

### 8. 性能测试（可选）

#### 8.1 批量上传测试

上传 10 个小文件（< 1MB），观察：
- 上传速度
- 目录创建是否正常
- 数据库记录是否正确

#### 8.2 文件列表查询测试

上传 100+ 文件后，访问文件列表页面：
- 页面加载时间 < 2 秒
- 分页功能正常

---

## 🐛 常见问题排查

### Q1: 启动时提示 "Column 'role' not found"

**原因**: Liquibase 变更集未执行

**解决**:
```sql
-- 检查 DATABASECHANGELOG 表
SELECT * FROM DATABASECHANGELOG ORDER BY DATEEXECUTED DESC;

-- 如果没有 003-add-user-fields，手动执行或重启应用
```

### Q2: 上传文件时提示 "不支持的文件类型"

**原因**: 文件扩展名不在白名单中

**解决**:
1. 检查文件扩展名是否在 `file.allowed-extensions` 配置中
2. 如果需要添加新类型，修改 `application.properties`

### Q3: 用户目录没有自动创建

**原因**: 可能是权限问题或路径配置错误

**排查**:
```bash
# 检查 uploads 目录权限
ls -la uploads/

# Windows 下检查
icacls uploads
```

**解决**:
- 确保应用对 uploads 目录有写入权限
- 检查日志中的详细错误信息

### Q4: 下载文件时提示 "文件不存在"

**原因**: 文件路径可能不正确

**排查**:
```sql
-- 检查数据库中的路径
SELECT file_path FROM file_records WHERE id = 1;

-- 验证物理文件是否存在
ls uploads/user_1/<filename>
```

---

## ✅ 验收标准

完成以下所有检查点，Phase 1 测试通过：

### 数据库
- [ ] users 表包含 role、enabled、last_login_at 字段
- [ ] 4 个新索引创建成功
- [ ] admin 用户角色为 ADMIN

### 配置
- [ ] 应用启动无错误
- [ ] 日志显示支持的文件类型
- [ ] 配置文件正确加载

### 功能
- [ ] 用户目录自动创建
- [ ] 至少成功上传 3 种不同类型的文件
- [ ] 禁止的文件类型被正确拦截
- [ ] 文件大小限制生效
- [ ] 文件下载正常
- [ ] 文件删除正常（物理文件 + 数据库记录）

### 安全
- [ ] 路径遍历攻击被阻止
- [ ] 跨用户访问被阻止
- [ ] 文件路径使用相对路径存储

### 性能
- [ ] 文件列表加载流畅
- [ ] 上传响应时间合理

---

## 📊 测试报告模板

```
测试日期: ___________
测试人员: ___________

数据库验证: □ 通过  □ 失败  备注: ___________
配置加载:   □ 通过  □ 失败  备注: ___________
目录创建:   □ 通过  □ 失败  备注: ___________
文件上传:   □ 通过  □ 失败  备注: ___________
文件下载:   □ 通过  □ 失败  备注: ___________
文件删除:   □ 通过  □ 失败  备注: ___________
安全防护:   □ 通过  □ 失败  备注: ___________

发现的问题:
1. ________________________________
2. ________________________________

总体评价: □ 通过  □ 不通过
```

---

## 🚀 下一步

测试通过后，可以继续 Phase 2 的开发：
- 账号管理模块
- 个人信息页面
- 密码修改功能

---

**文档版本**: 1.0  
**创建日期**: 2026-06-11  
**用途**: Phase 1 功能验证

