package net.docn.fileservice.controller;

import net.docn.fileservice.entity.FileRecord;
import net.docn.fileservice.entity.User;
import net.docn.fileservice.service.CustomUserDetails;
import net.docn.fileservice.service.FileStorageService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

@Controller
@RequestMapping("/admin")
public class AdminDashboardController {

    private static final Logger log = LoggerFactory.getLogger(AdminDashboardController.class);

    @Autowired
    private FileStorageService fileStorageService;

    /**
     * 管理员主页（控制台）
     */
    @GetMapping("/dashboard")
    public String dashboard(Model model, Authentication authentication) {
        Long currentUserId = getCurrentUserId(authentication);
        
        if (currentUserId == null) {
            return "redirect:/login";
        }

        try {
            // 获取当前用户名
            String username = authentication.getName();
            model.addAttribute("username", username);

            // 获取统计数据
            List<User> allUsers = fileStorageService.getAllUsers();
            List<FileRecord> allFiles = fileStorageService.getAllFiles();
            
            long totalUsers = allUsers.size();
            long totalFiles = allFiles.size();
            long totalStorage = allFiles.stream()
                    .mapToLong(file -> file.getFileSize() != null ? file.getFileSize() : 0)
                    .sum();

            model.addAttribute("totalUsers", totalUsers);
            model.addAttribute("totalFiles", totalFiles);
            model.addAttribute("totalStorage", formatSize(totalStorage));

        } catch (Exception e) {
            log.error("加载管理员控制台失败", e);
            return "redirect:/files";
        }

        return "admin/dashboard";
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
    private String formatSize(long bytes) {
        if (bytes < 1024) {
            return bytes + " B";
        } else if (bytes < 1024 * 1024) {
            return String.format("%.2f KB", bytes / 1024.0);
        } else if (bytes < 1024 * 1024 * 1024) {
            return String.format("%.2f MB", bytes / (1024.0 * 1024));
        } else {
            return String.format("%.2f GB", bytes / (1024.0 * 1024 * 1024));
        }
    }
}
