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
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/admin/files")
public class AdminFileController {

    private static final Logger log = LoggerFactory.getLogger(AdminFileController.class);

    @Autowired
    private FileStorageService fileStorageService;

    /**
     * 文件管理页面
     */
    @GetMapping
    public String filesPage(@RequestParam(defaultValue = "0") int page,
                           @RequestParam(defaultValue = "20") int size,
                           @RequestParam(required = false) String usernameSearch,
                           Model model, Authentication authentication) {
        Long currentUserId = getCurrentUserId(authentication);
        
        if (currentUserId == null) {
            return "redirect:/login";
        }

        try {
            // 获取所有用户（用于显示用户名）
            List<User> allUsers = fileStorageService.getAllUsers();
            model.addAttribute("users", allUsers);
            
            // 获取分页文件列表
            org.springframework.data.domain.Page<FileRecord> filePage = fileStorageService.getAllFiles(page, size);
            model.addAttribute("files", filePage.getContent());
            model.addAttribute("currentPage", page);
            model.addAttribute("totalPages", filePage.getTotalPages());
            model.addAttribute("totalElements", filePage.getTotalElements());
            model.addAttribute("pageSize", size);
            
            // 如果有搜索条件，过滤文件
            if (usernameSearch != null && !usernameSearch.trim().isEmpty()) {
                // 找到匹配的用户
                List<Long> matchingUserIds = allUsers.stream()
                    .filter(u -> u.getUsername().toLowerCase().contains(usernameSearch.toLowerCase()))
                    .map(User::getId)
                    .collect(java.util.stream.Collectors.toList());
                
                // 过滤文件
                List<FileRecord> allFiles = filePage.getContent();
                List<FileRecord> filteredFiles = allFiles.stream()
                    .filter(f -> matchingUserIds.contains(f.getUserId()))
                    .collect(java.util.stream.Collectors.toList());
                
                model.addAttribute("files", filteredFiles);
                model.addAttribute("usernameSearch", usernameSearch);
            }
            
            // 获取统计信息
            Map<String, Object> stats = fileStorageService.getFileStatistics();
            model.addAttribute("stats", stats);
            
            model.addAttribute("currentUserId", currentUserId);
        } catch (Exception e) {
            log.error("获取文件列表失败", e);
            return "redirect:/files";
        }

        return "admin/files";
    }

    /**
     * 删除文件（管理员）
     */
    @PostMapping("/delete/{id}")
    public String deleteFile(@PathVariable Long id,
                            Authentication authentication,
                            RedirectAttributes redirectAttributes) {
        try {
            Long currentUserId = getCurrentUserId(authentication);
            if (currentUserId == null) {
                redirectAttributes.addFlashAttribute("error", "用户未登录");
                return "redirect:/login";
            }

            fileStorageService.deleteFileAsAdmin(id);
            redirectAttributes.addFlashAttribute("message", "文件删除成功");
            
        } catch (Exception e) {
            log.error("删除文件失败", e);
            redirectAttributes.addFlashAttribute("error", "删除文件失败，请稍后重试");
        }

        return "redirect:/admin/files";
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
}
