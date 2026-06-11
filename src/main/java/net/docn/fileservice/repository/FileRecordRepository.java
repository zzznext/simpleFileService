package net.docn.fileservice.repository;

import net.docn.fileservice.entity.FileRecord;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface FileRecordRepository extends JpaRepository<FileRecord, Long> {
    List<FileRecord> findByUserIdOrderByUploadedAtDesc(Long userId);
    
    @Query("SELECT COALESCE(SUM(f.fileSize), 0) FROM FileRecord f WHERE f.userId = :userId")
    Long getTotalFileSizeByUserId(Long userId);
    
    // 获取所有文件（按上传时间降序）
    List<FileRecord> findAllByOrderByUploadedAtDesc();
    
    // 分页查询所有文件
    Page<FileRecord> findAllByOrderByUploadedAtDesc(Pageable pageable);
    
    // 统计总文件数
    long count();
    
    // 统计总存储大小
    @Query("SELECT COALESCE(SUM(f.fileSize), 0) FROM FileRecord f")
    Long getTotalFileSize();
}
