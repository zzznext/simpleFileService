package net.docn.fileservice.repository;

import net.docn.fileservice.entity.FileRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface FileRecordRepository extends JpaRepository<FileRecord, Long> {
    List<FileRecord> findByUserIdOrderByUploadedAtDesc(Long userId);
    
    @Query("SELECT COALESCE(SUM(f.fileSize), 0) FROM FileRecord f WHERE f.userId = :userId")
    Long getTotalFileSizeByUserId(Long userId);
}
