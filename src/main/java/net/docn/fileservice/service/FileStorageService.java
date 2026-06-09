package net.docn.fileservice.service;

import jakarta.annotation.PostConstruct;
import jakarta.transaction.Transactional;
import net.docn.fileservice.config.FileStorageProperties;
import net.docn.fileservice.entity.FileRecord;
import net.docn.fileservice.repository.FileRecordRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.*;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class FileStorageService {

    private static final Logger log = LoggerFactory.getLogger(FileStorageService.class);

    @Autowired
    private FileStorageProperties fileStorageProperties;

    @Autowired
    private FileRecordRepository fileRecordRepository;

    private Path rootLocation;

    /**
     * 初始化存储目录
     */
    @PostConstruct
    public void init() {
        this.rootLocation = Paths.get(fileStorageProperties.getUploadDir()).normalize().toAbsolutePath();
        try {
            Files.createDirectories(rootLocation);
        } catch (IOException e) {
            throw new RuntimeException("无法初始化存储目录", e);
        }
        log.info("文件存储目录初始化完成: {}", rootLocation);
    }

    /**
     * 上传文件
     */
    @Transactional
    public FileRecord uploadFile(MultipartFile file, Long userId) throws IOException {
        // 检查是否为zip文件
        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null || !originalFilename.toLowerCase().endsWith(".zip")) {
            throw new IllegalArgumentException("只支持上传ZIP文件");
        }

        // 检查容量限制
        Long currentUsage = fileRecordRepository.getTotalFileSizeByUserId(userId);
        long newSize = file.getSize();
        
        if (currentUsage + newSize > fileStorageProperties.getMaxStorageSize()) {
            throw new IllegalStateException("容量超限！当前已用: " + formatSize(currentUsage) 
                    + ", 可用: " + formatSize(fileStorageProperties.getMaxStorageSize() - currentUsage));
        }

        // 生成唯一文件名
        String uniqueFilename = UUID.randomUUID().toString() + "_" + originalFilename;
        Path destinationFile = rootLocation.resolve(uniqueFilename).normalize().toAbsolutePath();

        // 确保文件保存在正确的目录下（防止目录遍历攻击）
        if (!destinationFile.startsWith(rootLocation)) {
            log.error("路径安全检查失败！");
            log.error("目标文件路径: {}", destinationFile);
            log.error("根目录路径: {}", rootLocation);
            throw new IOException("无法保存文件：无效的文件路径");
        }

        // 保存文件
        Files.copy(file.getInputStream(), destinationFile);

        // 创建文件记录
        FileRecord fileRecord = new FileRecord();
        fileRecord.setUserId(userId);
        fileRecord.setFilename(uniqueFilename);
        fileRecord.setOriginalFilename(originalFilename);
        fileRecord.setFileSize(file.getSize());
        fileRecord.setFilePath(destinationFile.toString());
        fileRecord.setContentType(file.getContentType());
        fileRecord.setUploadedAt(LocalDateTime.now());

        return fileRecordRepository.save(fileRecord);
    }

    /**
     * 下载文件
     */
    public Path downloadFile(String filename) {
        // 防止目录遍历攻击
        Path filePath = rootLocation.resolve(filename).normalize();
        
        // 验证文件路径在允许的目录内
        if (!filePath.startsWith(rootLocation)) {
            log.error("非法的文件访问尝试: {}", filename);
            throw new RuntimeException("非法的文件访问");
        }
        
        if (!Files.exists(filePath)) {
            throw new RuntimeException("文件不存在");
        }

        return filePath;
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

        // 删除物理文件
        try {
            Path filePath = Paths.get(fileRecord.getFilePath());
            Files.deleteIfExists(filePath);
        } catch (IOException e) {
            throw new RuntimeException("删除文件失败", e);
        }

        // 删除数据库记录
        fileRecordRepository.deleteById(fileId);
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
}
