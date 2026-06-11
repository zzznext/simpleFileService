package net.docn.fileservice.service;

import jakarta.annotation.PostConstruct;
import jakarta.transaction.Transactional;
import net.docn.fileservice.config.FileStorageProperties;
import net.docn.fileservice.entity.FileRecord;
import net.docn.fileservice.entity.User;
import net.docn.fileservice.repository.FileRecordRepository;
import net.docn.fileservice.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.*;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.Random;
import java.util.HashMap;
import java.util.Map;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

@Service
public class FileStorageService {

    private static final Logger log = LoggerFactory.getLogger(FileStorageService.class);

    @Autowired
    private FileStorageProperties fileStorageProperties;

    @Autowired
    private FileRecordRepository fileRecordRepository;
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private PasswordEncoder passwordEncoder;

    private Path rootLocation;

    /**
     * 初始化存储目录
     */
    @PostConstruct
    public void init() {
        this.rootLocation = Paths.get(fileStorageProperties.getUploadDir()).normalize().toAbsolutePath();
        try {
            Files.createDirectories(rootLocation);
            log.info("文件存储根目录初始化完成: {}", rootLocation);
            
            // 如果配置了允许的文件扩展名，记录日志
            if (!fileStorageProperties.getAllowedExtensions().isEmpty()) {
                log.info("支持的文件类型: {}", String.join(", ", fileStorageProperties.getAllowedExtensions()));
            }
        } catch (IOException e) {
            throw new RuntimeException("无法初始化存储目录", e);
        }
    }

    /**
     * 获取或创建用户专属目录
     * @param userId 用户ID
     * @return 用户目录路径
     */
    public Path getUserDirectory(Long userId) {
        Path userDir = rootLocation.resolve("user_" + userId).normalize();
        
        try {
            if (!Files.exists(userDir)) {
                Files.createDirectories(userDir);
                log.info("为用户 {} 创建目录: {}", userId, userDir);
            }
        } catch (IOException e) {
            log.error("为用户 {} 创建目录失败", userId, e);
            throw new RuntimeException("无法创建用户目录", e);
        }
        
        return userDir;
    }

    /**
     * 上传文件
     */
    @Transactional
    public FileRecord uploadFile(MultipartFile file, Long userId) throws IOException {
        // 1. 验证文件类型
        validateFileType(file);

        // 2. 检查容量限制
        Long currentUsage = fileRecordRepository.getTotalFileSizeByUserId(userId);
        long newSize = file.getSize();
        
        if (currentUsage + newSize > fileStorageProperties.getMaxStorageSize()) {
            throw new IllegalStateException("容量超限！当前已用: " + formatSize(currentUsage) 
                    + ", 可用: " + formatSize(fileStorageProperties.getMaxStorageSize() - currentUsage));
        }

        // 3. 获取用户专属目录
        Path userDir = getUserDirectory(userId);

        // 4. 生成唯一文件名
        String originalFilename = file.getOriginalFilename();
        String uniqueFilename = UUID.randomUUID().toString() + "_" + originalFilename;
        Path destinationFile = userDir.resolve(uniqueFilename).normalize().toAbsolutePath();

        // 5. 确保文件保存在用户目录下（防止目录遍历攻击）
        if (!destinationFile.startsWith(userDir)) {
            log.error("路径安全检查失败！");
            log.error("目标文件路径: {}", destinationFile);
            log.error("用户目录: {}", userDir);
            throw new IOException("无法保存文件：无效的文件路径");
        }

        // 6. 保存文件
        Files.copy(file.getInputStream(), destinationFile);

        // 7. 创建文件记录（使用相对路径）
        FileRecord fileRecord = new FileRecord();
        fileRecord.setUserId(userId);
        fileRecord.setFilename(uniqueFilename);
        fileRecord.setOriginalFilename(originalFilename);
        fileRecord.setFileSize(file.getSize());
        fileRecord.setFilePath("user_" + userId + "/" + uniqueFilename); // 相对路径
        fileRecord.setContentType(file.getContentType());
        fileRecord.setUploadedAt(LocalDateTime.now());

        FileRecord savedRecord = fileRecordRepository.save(fileRecord);
        log.info("文件上传成功: {} (用户: {}, 大小: {})", originalFilename, userId, formatSize(newSize));
        
        return savedRecord;
    }

    /**
     * 验证文件类型
     */
    private void validateFileType(MultipartFile file) {
        String originalFilename = file.getOriginalFilename();
        
        if (originalFilename == null || originalFilename.isEmpty()) {
            throw new IllegalArgumentException("文件名不能为空");
        }

        // 检查文件扩展名
        String extension = getFileExtension(originalFilename).toLowerCase();
        List<String> allowedExtensions = fileStorageProperties.getAllowedExtensions();
        
        if (!allowedExtensions.isEmpty() && !allowedExtensions.contains(extension)) {
            log.warn("不支持的文件类型: {}", extension);
            throw new IllegalArgumentException("不支持的文件类型: " + extension + 
                "。支持的类型: " + String.join(", ", allowedExtensions));
        }

        // 检查文件大小
        long maxFileSize = fileStorageProperties.getMaxFileSize();
        if (maxFileSize > 0 && file.getSize() > maxFileSize) {
            throw new IllegalArgumentException("文件大小超过限制（最大" + formatSize(maxFileSize) + "）");
        }

        // 可选：检查 MIME 类型
        String contentType = file.getContentType();
        if (contentType != null) {
            List<String> allowedTypes = fileStorageProperties.getAllowedTypes();
            if (!allowedTypes.isEmpty() && !allowedTypes.contains(contentType.toLowerCase())) {
                log.warn("文件MIME类型不在白名单中: {}", contentType);
                // 仅警告，不阻止上传（因为某些浏览器可能发送错误的MIME类型）
            }
        }
    }

    /**
     * 获取文件扩展名
     */
    private String getFileExtension(String filename) {
        int lastDotIndex = filename.lastIndexOf(".");
        if (lastDotIndex == -1) {
            return "";
        }
        return filename.substring(lastDotIndex);
    }

    /**
     * 下载文件
     */
    public Path downloadFile(String filename) {
        log.info("尝试下载文件: {}", filename);
        log.info("rootLocation: {}", rootLocation);
        
        // 清理路径：移除开头的斜杠
        String cleanFilename = filename;
        if (cleanFilename.startsWith("/") || cleanFilename.startsWith("\\")) {
            cleanFilename = cleanFilename.substring(1);
        }
        
        log.info("清理后的文件名: {}", cleanFilename);
        
        // 防止目录遍历攻击
        Path filePath = rootLocation.resolve(cleanFilename).normalize();
        log.info("解析后的完整路径: {}", filePath);
        
        // 验证文件路径在允许的目录内
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

    /**
     * 根据文件记录下载文件（推荐使用）
     */
    public Path downloadFileByRecord(FileRecord fileRecord) {
        String filePath = fileRecord.getFilePath();
        Path fullPath = rootLocation.resolve(filePath).normalize();
        
        // 验证文件路径在允许的目录内
        if (!fullPath.startsWith(rootLocation)) {
            log.error("非法的文件访问尝试: {}", filePath);
            throw new RuntimeException("非法的文件访问");
        }
        
        if (!Files.exists(fullPath)) {
            throw new RuntimeException("文件不存在: " + fileRecord.getOriginalFilename());
        }

        return fullPath;
    }

    /**
     * 删除文件
     */
    @Transactional
    public void deleteFile(Long fileId, Long userId) {
        FileRecord fileRecord = fileRecordRepository.findById(fileId)
                .orElseThrow(() -> new RuntimeException("文件记录不存在"));

        // 验证文件属于当前用户
        if (!fileRecord.getUserId().equals(userId)) {
            throw new RuntimeException("无权删除此文件");
        }

        // 删除物理文件（使用相对路径）
        try {
            Path filePath = rootLocation.resolve(fileRecord.getFilePath()).normalize();
            
            // 安全检查：确保文件在根目录下
            if (!filePath.startsWith(rootLocation)) {
                log.error("非法的文件删除尝试: {}", fileRecord.getFilePath());
                throw new RuntimeException("非法的文件路径");
            }
            
            boolean deleted = Files.deleteIfExists(filePath);
            if (deleted) {
                log.info("文件已删除: {}", fileRecord.getOriginalFilename());
            } else {
                log.warn("文件不存在，跳过删除: {}", fileRecord.getOriginalFilename());
            }
        } catch (IOException e) {
            log.error("删除文件失败: {}", fileRecord.getOriginalFilename(), e);
            throw new RuntimeException("删除文件失败", e);
        }

        // 删除数据库记录
        fileRecordRepository.deleteById(fileId);
        log.info("文件记录已删除: ID={}", fileId);
    }

    /**
     * 获取用户的文件列表
     */
    public List<FileRecord> getUserFiles(Long userId) {
        return fileRecordRepository.findByUserIdOrderByUploadedAtDesc(userId);
    }

    /**
     * 获取用户已用容量
     */
    public Long getUsedStorage(Long userId) {
        return fileRecordRepository.getTotalFileSizeByUserId(userId);
    }

    /**
     * 获取可用容量
     */
    public Long getAvailableStorage(Long userId) {
        Long used = getUsedStorage(userId);
        return fileStorageProperties.getMaxStorageSize() - used;
    }

    /**
     * 获取最大存储容量
     */
    public Long getMaxStorage() {
        return fileStorageProperties.getMaxStorageSize();
    }

    /**
     * 格式化文件大小
     */
    private String formatSize(long size) {
        if (size < 1024) {
            return size + " B";
        } else if (size < 1024 * 1024) {
            return String.format("%.2f KB", size / 1024.0);
        } else if (size < 1024 * 1024 * 1024) {
            return String.format("%.2f MB", size / (1024.0 * 1024));
        } else {
            return String.format("%.2f GB", size / (1024.0 * 1024 * 1024));
        }
    }

    /**
     * 获取下载URL
     */
    public String getDownloadUrl(String filename) {
        return fileStorageProperties.getDownloadUrlPrefix() + filename;
    }

    /**
     * 删除用户的所有文件和目录（管理员功能）
     * @param userId 用户ID
     */
    public void deleteUserDirectory(Long userId) {
        Path userDir = rootLocation.resolve("user_" + userId).normalize();
        
        // 安全检查
        if (!userDir.startsWith(rootLocation)) {
            log.error("非法的用户目录路径: {}", userDir);
            throw new RuntimeException("非法的用户目录路径");
        }
        
        try {
            if (Files.exists(userDir)) {
                // 递归删除目录及其所有内容
                Files.walk(userDir)
                    .sorted((a, b) -> b.compareTo(a)) // 先删除文件，再删除目录
                    .map(Path::toFile)
                    .forEach(java.io.File::delete);
                log.info("已删除用户 {} 的目录: {}", userId, userDir);
            } else {
                log.warn("用户目录不存在，跳过删除: {}", userDir);
            }
        } catch (IOException e) {
            log.error("删除用户目录失败: userId={}", userId, e);
            throw new RuntimeException("删除用户目录失败", e);
        }
    }
    
    // ==================== 账号管理方法 ====================
    
    /**
     * 修改密码
     * @param userId 用户ID
     * @param oldPassword 旧密码
     * @param newPassword 新密码
     */
    @Transactional
    public void changePassword(Long userId, String oldPassword, String newPassword) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("用户不存在"));
        
        // 验证旧密码
        if (!passwordEncoder.matches(oldPassword, user.getPassword())) {
            log.warn("用户 {} 的旧密码错误", user.getUsername());
            throw new IllegalArgumentException("当前密码错误");
        }
        
        // 验证新密码强度
        validatePasswordStrength(newPassword);
        
        // 更新密码
        user.setPassword(passwordEncoder.encode(newPassword));
        user.setUpdatedAt(LocalDateTime.now());
        userRepository.save(user);
        
        log.info("用户 {} 修改密码成功", user.getUsername());
    }
    
    /**
     * 修改邮箱
     * @param userId 用户ID
     * @param newEmail 新邮箱
     */
    @Transactional
    public void changeEmail(Long userId, String newEmail) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("用户不存在"));
        
        // 验证邮箱格式
        if (newEmail == null || !newEmail.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$")) {
            throw new IllegalArgumentException("邮箱格式不正确");
        }
        
        // 检查邮箱是否已被使用
        if (userRepository.existsByEmail(newEmail)) {
            throw new IllegalArgumentException("该邮箱已被其他用户使用");
        }
        
        // 更新邮箱
        String oldEmail = user.getEmail();
        user.setEmail(newEmail);
        user.setUpdatedAt(LocalDateTime.now());
        userRepository.save(user);
        
        log.info("用户 {} 修改邮箱: {} -> {}", user.getUsername(), oldEmail, newEmail);
    }
    
    /**
     * 获取用户信息
     * @param userId 用户ID
     * @return 用户对象
     */
    public User getUserInfo(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("用户不存在"));
    }
    
    /**
     * 验证密码强度
     * @param password 密码
     */
    private void validatePasswordStrength(String password) {
        if (password == null || password.length() < 6) {
            throw new IllegalArgumentException("密码长度至少为6位");
        }
        
        if (password.length() > 100) {
            throw new IllegalArgumentException("密码长度不能超过100位");
        }
        
        // 至少包含字母和数字
        boolean hasLetter = false;
        boolean hasDigit = false;
        for (char c : password.toCharArray()) {
            if (Character.isLetter(c)) hasLetter = true;
            if (Character.isDigit(c)) hasDigit = true;
        }
        
        if (!hasLetter || !hasDigit) {
            throw new IllegalArgumentException("密码必须同时包含字母和数字");
        }
    }
    
    // ==================== 管理员用户管理方法 ====================
    
    /**
     * 获取所有用户列表
     */
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }
    
    /**
     * 创建新用户
     * @param username 用户名
     * @param password 密码（明文）
     * @param email 邮箱
     * @param role 角色（USER 或 ADMIN）
     * @return 创建的用户
     */
    @Transactional
    public User createUser(String username, String password, String email, String role) {
        // 检查用户名是否已存在
        if (userRepository.existsByUsername(username)) {
            throw new IllegalArgumentException("用户名已存在");
        }
        
        // 检查邮箱是否已被使用
        if (email != null && !email.isEmpty() && userRepository.existsByEmail(email)) {
            throw new IllegalArgumentException("邮箱已被使用");
        }
        
        // 验证角色
        if (!"USER".equals(role) && !"ADMIN".equals(role)) {
            throw new IllegalArgumentException("无效的角色");
        }
        
        // 创建用户
        User user = new User();
        user.setUsername(username);
        user.setPassword(passwordEncoder.encode(password));
        user.setEmail(email);
        user.setRole(role);
        user.setEnabled(true);
        user.setCreatedAt(LocalDateTime.now());
        user.setUpdatedAt(LocalDateTime.now());
        
        User savedUser = userRepository.save(user);
        log.info("创建用户成功: {} (角色: {})", username, role);
        
        return savedUser;
    }
    
    /**
     * 删除用户及其所有文件
     * @param userId 要删除的用户ID
     * @param currentUserId 当前操作者ID（防止删除自己）
     */
    @Transactional
    public void deleteUser(Long userId, Long currentUserId) {
        // 防止删除自己
        if (userId.equals(currentUserId)) {
            throw new IllegalArgumentException("不能删除自己的账号");
        }
        
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("用户不存在"));
        
        // 先删除用户的所有文件
        List<FileRecord> userFiles = fileRecordRepository.findByUserIdOrderByUploadedAtDesc(userId);
        for (FileRecord fileRecord : userFiles) {
            try {
                Path filePath = rootLocation.resolve(fileRecord.getFilePath()).normalize();
                Files.deleteIfExists(filePath);
                log.info("删除用户文件: {}", fileRecord.getOriginalFilename());
            } catch (IOException e) {
                log.warn("删除文件失败: {}", fileRecord.getOriginalFilename(), e);
            }
        }
        
        // 删除用户的数据库记录
        fileRecordRepository.deleteAll(userFiles);
        
        // 删除用户目录
        deleteUserDirectory(userId);
        
        // 删除用户
        userRepository.delete(user);
        log.info("删除用户成功: {} (ID: {})", user.getUsername(), userId);
    }
    
    /**
     * 禁用/启用用户
     * @param userId 用户ID
     * @param enabled true=启用，false=禁用
     */
    @Transactional
    public void setUserEnabled(Long userId, boolean enabled) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("用户不存在"));
        
        user.setEnabled(enabled);
        user.setUpdatedAt(LocalDateTime.now());
        userRepository.save(user);
        
        log.info("用户 {} 状态已{}", user.getUsername(), enabled ? "启用" : "禁用");
    }
    
    /**
     * 重置用户密码
     * @param userId 用户ID
     * @param newPassword 新密码
     */
    @Transactional
    public void resetPassword(Long userId, String newPassword) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("用户不存在"));
        
        // 验证密码强度
        validatePasswordStrength(newPassword);
        
        user.setPassword(passwordEncoder.encode(newPassword));
        user.setUpdatedAt(LocalDateTime.now());
        userRepository.save(user);
        
        log.info("重置用户密码成功: {}", user.getUsername());
    }
    
    /**
     * 生成随机密码
     * @param length 密码长度
     * @return 随机密码
     */
    public String generateRandomPassword(int length) {
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
        Random random = new Random();
        StringBuilder password = new StringBuilder();
        
        for (int i = 0; i < length; i++) {
            password.append(chars.charAt(random.nextInt(chars.length())));
        }
        
        return password.toString();
    }
    
    // ==================== 管理员文件管理方法 ====================
    
    /**
     * 获取所有文件（管理员）- 分页
     */
    public Page<FileRecord> getAllFiles(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return fileRecordRepository.findAllByOrderByUploadedAtDesc(pageable);
    }
    
    /**
     * 删除任意文件（管理员）
     * @param fileId 文件ID
     */
    @Transactional
    public void deleteFileAsAdmin(Long fileId) {
        FileRecord fileRecord = fileRecordRepository.findById(fileId)
                .orElseThrow(() -> new RuntimeException("文件记录不存在"));
        
        // 删除物理文件
        try {
            Path filePath = rootLocation.resolve(fileRecord.getFilePath()).normalize();
            
            // 安全检查
            if (!filePath.startsWith(rootLocation)) {
                log.error("非法的文件删除尝试: {}", fileRecord.getFilePath());
                throw new RuntimeException("非法的文件路径");
            }
            
            boolean deleted = Files.deleteIfExists(filePath);
            if (deleted) {
                log.info("管理员删除文件: {}", fileRecord.getOriginalFilename());
            } else {
                log.warn("文件不存在，跳过删除: {}", fileRecord.getOriginalFilename());
            }
        } catch (IOException e) {
            log.error("删除文件失败: {}", fileRecord.getOriginalFilename(), e);
            throw new RuntimeException("删除文件失败", e);
        }
        
        // 删除数据库记录
        fileRecordRepository.deleteById(fileId);
        log.info("文件记录已删除: ID={}", fileId);
    }
    
    /**
     * 获取文件统计信息
     */
    public Map<String, Object> getFileStatistics() {
        Map<String, Object> stats = new HashMap<>();
        
        // 总文件数
        long totalFiles = fileRecordRepository.count();
        stats.put("totalFiles", totalFiles);
        
        // 总存储大小
        Long totalSize = fileRecordRepository.getTotalFileSize();
        stats.put("totalSize", totalSize != null ? totalSize : 0L);
        stats.put("totalSizeFormatted", formatSize(totalSize != null ? totalSize : 0L));
        
        // 用户数量
        long totalUsers = userRepository.count();
        stats.put("totalUsers", totalUsers);
        
        return stats;
    }
}
