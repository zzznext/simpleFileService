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
            
            // 判断是否为管理员
            if (authentication != null && authentication.getPrincipal() instanceof CustomUserDetails) {
                CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
                model.addAttribute("isAdmin", userDetails.isAdmin());
            } else {
                model.addAttribute("isAdmin", false);
            }
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
    @GetMapping("/download/{*filePath}")
    public ResponseEntity<Resource> downloadFile(@PathVariable String filePath) {
        try {
            log.info("下载请求: {}", filePath);
            
            Path file = fileStorageService.downloadFile(filePath);
            Resource resource = new UrlResource(file.toUri());

            if (!resource.exists() || !resource.isReadable()) {
                log.warn("文件不存在或不可读: {}", filePath);
                return ResponseEntity.notFound().build();
            }

            // 从路径中提取原始文件名
            String originalFilename = filePath.substring(filePath.indexOf("/") + 1);
            // 去掉 UUID 前缀
            if (originalFilename.contains("_")) {
                originalFilename = originalFilename.substring(originalFilename.indexOf("_") + 1);
            }
            
            // 根据文件扩展名确定 Content-Type
            MediaType contentType = getMediaTypeByFilename(originalFilename);
            
            // URL 编码文件名，支持中文
            String encodedFilename = java.net.URLEncoder.encode(originalFilename, "UTF-8")
                .replaceAll("\\+", "%20");

            return ResponseEntity.ok()
                    .contentType(contentType)
                    .header(HttpHeaders.CONTENT_DISPOSITION, 
                        "attachment; filename=\"" + encodedFilename + "\"; filename*=UTF-8''" + encodedFilename)
                    .body(resource);
        } catch (Exception e) {
            log.error("下载文件失败", e);
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * 根据文件名确定 Media Type
     */
    private MediaType getMediaTypeByFilename(String filename) {
        String extension = "";
        int lastDotIndex = filename.lastIndexOf(".");
        if (lastDotIndex > 0) {
            extension = filename.substring(lastDotIndex + 1).toLowerCase();
        }
        
        switch (extension) {
            case "jpg":
            case "jpeg":
                return MediaType.IMAGE_JPEG;
            case "png":
                return MediaType.parseMediaType("image/png");
            case "gif":
                return MediaType.parseMediaType("image/gif");
            case "webp":
                return MediaType.parseMediaType("image/webp");
            case "pdf":
                return MediaType.parseMediaType("application/pdf");
            case "zip":
                return MediaType.parseMediaType("application/zip");
            case "doc":
                return MediaType.parseMediaType("application/msword");
            case "docx":
                return MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.wordprocessingml.document");
            case "txt":
                return MediaType.TEXT_PLAIN;
            case "csv":
                return MediaType.parseMediaType("text/csv");
            default:
                return MediaType.APPLICATION_OCTET_STREAM;
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
