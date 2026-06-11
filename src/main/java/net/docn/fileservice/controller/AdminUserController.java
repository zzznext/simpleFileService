package net.docn.fileservice.controller;

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

@Controller
@RequestMapping("/admin")
public class AdminUserController {

    private static final Logger log = LoggerFactory.getLogger(AdminUserController.class);

    @Autowired
    private FileStorageService fileStorageService;

    /**
     * 用户管理页面
     */
    @GetMapping("/users")
    public String usersPage(Model model, Authentication authentication) {
        Long currentUserId = getCurrentUserId(authentication);
        
        if (currentUserId == null) {
            return "redirect:/login";
        }

        try {
            List<User> users = fileStorageService.getAllUsers();
            model.addAttribute("users", users);
            model.addAttribute("currentUserId", currentUserId);
        } catch (Exception e) {
            log.error("获取用户列表失败", e);
            return "redirect:/files";
        }

        return "admin/users";
    }

    /**
     * 创建新用户
     */
    @PostMapping("/users/create")
    public String createUser(@RequestParam String username,
                            @RequestParam String password,
                            @RequestParam(required = false) String email,
                            @RequestParam(defaultValue = "USER") String role,
                            Authentication authentication,
                            RedirectAttributes redirectAttributes) {
        try {
            Long currentUserId = getCurrentUserId(authentication);
            if (currentUserId == null) {
                redirectAttributes.addFlashAttribute("error", "用户未登录");
                return "redirect:/login";
            }

            fileStorageService.createUser(username, password, email, role);
            redirectAttributes.addFlashAttribute("message", "用户创建成功");
            
        } catch (IllegalArgumentException e) {
            log.warn("创建用户参数错误: {}", e.getMessage());
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        } catch (Exception e) {
            log.error("创建用户失败", e);
            redirectAttributes.addFlashAttribute("error", "创建用户失败，请稍后重试");
        }

        return "redirect:/admin/users";
    }

    /**
     * 删除用户
     */
    @PostMapping("/users/delete/{id}")
    public String deleteUser(@PathVariable Long id,
                            Authentication authentication,
                            RedirectAttributes redirectAttributes) {
        try {
            Long currentUserId = getCurrentUserId(authentication);
            if (currentUserId == null) {
                redirectAttributes.addFlashAttribute("error", "用户未登录");
                return "redirect:/login";
            }

            fileStorageService.deleteUser(id, currentUserId);
            redirectAttributes.addFlashAttribute("message", "用户删除成功");
            
        } catch (IllegalArgumentException e) {
            log.warn("删除用户参数错误: {}", e.getMessage());
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        } catch (Exception e) {
            log.error("删除用户失败", e);
            redirectAttributes.addFlashAttribute("error", "删除用户失败，请稍后重试");
        }

        return "redirect:/admin/users";
    }

    /**
     * 禁用/启用用户
     */
    @PostMapping("/users/toggle-status/{id}")
    public String toggleUserStatus(@PathVariable Long id,
                                  Authentication authentication,
                                  RedirectAttributes redirectAttributes) {
        try {
            Long currentUserId = getCurrentUserId(authentication);
            if (currentUserId == null) {
                redirectAttributes.addFlashAttribute("error", "用户未登录");
                return "redirect:/login";
            }

            // 获取用户当前状态
            User user = fileStorageService.getUserInfo(id);
            boolean newStatus = !user.getEnabled();
            
            fileStorageService.setUserEnabled(id, newStatus);
            redirectAttributes.addFlashAttribute("message", 
                newStatus ? "用户已启用" : "用户已禁用");
            
        } catch (Exception e) {
            log.error("切换用户状态失败", e);
            redirectAttributes.addFlashAttribute("error", "操作失败，请稍后重试");
        }

        return "redirect:/admin/users";
    }

    /**
     * 重置用户密码
     */
    @PostMapping("/users/reset-password/{id}")
    public String resetPassword(@PathVariable Long id,
                               Authentication authentication,
                               RedirectAttributes redirectAttributes) {
        try {
            Long currentUserId = getCurrentUserId(authentication);
            if (currentUserId == null) {
                redirectAttributes.addFlashAttribute("error", "用户未登录");
                return "redirect:/login";
            }

            // 生成随机密码
            String newPassword = fileStorageService.generateRandomPassword(10);
            fileStorageService.resetPassword(id, newPassword);
            
            redirectAttributes.addFlashAttribute("message", 
                "密码重置成功！新密码: " + newPassword);
            
        } catch (Exception e) {
            log.error("重置密码失败", e);
            redirectAttributes.addFlashAttribute("error", "重置密码失败，请稍后重试");
        }

        return "redirect:/admin/users";
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
