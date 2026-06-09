package net.docn.fileservice.controller;

import net.docn.fileservice.entity.FileRecord;
import net.docn.fileservice.service.CustomUserDetails;
import net.docn.fileservice.service.FileStorageService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.nio.file.Path;
import java.util.List;

@Controller
public class FileController {

    private static final Logger log = LoggerFactory.getLogger(FileController.class);

    @Autowired
    private FileStorageService fileStorageService;

    /**
     * 登录页面
     */
    @GetMapping("/login")
    public String loginPage() {
        return "login";
    }

    /**
     * 文件管理页面
     */
    @GetMapping("/files")
    public String filesPage(Model model, Authentication authentication) {
        Long userId = getCurrentUserId(authentication);
        
        if (userId != null) {
            List<FileRecord> files = fileStorageService.getUserFiles(userId);
            Long usedStorage = fileStorageService.getUsedStorage(userId);
            Long availableStorage = fileStorageService.getAvailableStorage(userId);
            Long maxStorage = fileStorageService.getMaxStorage();
            
            model.addAttribute("files", files);
            model.addAttribute("usedStorage", formatSize(usedStorage));
            model.addAttribute("availableStorage", formatSize(availableStorage));
            model.addAttribute("maxStorage", formatSize(maxStorage));
        }
        
        return "files";
    }

    /**
     * 上传文件
     */
    @PostMapping("/upload")
    public String uploadFile(@RequestParam("file") MultipartFile file, 
                            Authentication authentication,
                            RedirectAttributes redirectAttributes) {
        try {
            Long userId = getCurrentUserId(authentication);
            if (userId == null) {
                log.warn("用户未登录");
                redirectAttributes.addFlashAttribute("error", "用户未登录");
                return "redirect:/files";
            }

            FileRecord fileRecord = fileStorageService.uploadFile(file, userId);
            log.info("文件上传成功: {}", fileRecord.getOriginalFilename());
            redirectAttributes.addFlashAttribute("message", "文件上传成功");
        } catch (IllegalArgumentException e) {
            log.warn("上传参数错误: {}", e.getMessage());
            redirectAttributes.addFlashAttribute("error", "上传失败: " + e.getMessage());
        } catch (IllegalStateException e) {
            log.warn("容量超限: {}", e.getMessage());
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        } catch (Exception e) {
            log.error("上传失败", e);
            redirectAttributes.addFlashAttribute("error", "上传失败，请稍后重试");
        }
        
        return "redirect:/files";
    }

    /**
     * 删除文件
     */
    @PostMapping("/delete/{id}")
    public String deleteFile(@PathVariable Long id, 
                            Authentication authentication,
                            RedirectAttributes redirectAttributes) {
        try {
            Long userId = getCurrentUserId(authentication);
            if (userId == null) {
                redirectAttributes.addFlashAttribute("error", "用户未登录");
                return "redirect:/files";
            }

            fileStorageService.deleteFile(id, userId);
            redirectAttributes.addFlashAttribute("message", "文件删除成功");
        } catch (Exception e) {
            log.error("删除失败", e);
            redirectAttributes.addFlashAttribute("error", "删除失败，请稍后重试");
        }
        
        return "redirect:/files";
    }

    /**
     * 下载文件（无需登录）
     */
    @GetMapping("/download/{filename}")
    public ResponseEntity<Resource> downloadFile(@PathVariable String filename) {
        try {
            Path filePath = fileStorageService.downloadFile(filename);
            Resource resource = new UrlResource(filePath.toUri());

            if (!resource.exists() || !resource.isReadable()) {
                return ResponseEntity.notFound().build();
            }

            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType("application/zip"))
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + resource.getFilename() + "\"")
                    .body(resource);
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * 获取当前用户ID
     */
    private Long getCurrentUserId(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return null;
        }
        
        Object principal = authentication.getPrincipal();
        if (principal instanceof CustomUserDetails) {
            return ((CustomUserDetails) principal).getUserId();
        }
        
        return null;
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
}
