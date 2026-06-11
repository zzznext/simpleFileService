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

@Controller
@RequestMapping("/account")
public class AccountController {

    private static final Logger log = LoggerFactory.getLogger(AccountController.class);

    @Autowired
    private FileStorageService fileStorageService;

    /**
     * 个人信息页面
     */
    @GetMapping("/profile")
    public String profilePage(Model model, Authentication authentication) {
        Long userId = getCurrentUserId(authentication);
        
        if (userId == null) {
            return "redirect:/login";
        }

        try {
            User user = fileStorageService.getUserInfo(userId);
            model.addAttribute("user", user);
        } catch (Exception e) {
            log.error("获取用户信息失败", e);
            return "redirect:/files";
        }

        return "account/profile";
    }

    /**
     * 修改密码
     */
    @PostMapping("/change-password")
    public String changePassword(@RequestParam String oldPassword,
                                 @RequestParam String newPassword,
                                 @RequestParam String confirmPassword,
                                 Authentication authentication,
                                 RedirectAttributes redirectAttributes) {
        try {
            Long userId = getCurrentUserId(authentication);
            if (userId == null) {
                redirectAttributes.addFlashAttribute("error", "用户未登录");
                return "redirect:/login";
            }

            // 验证新密码一致性
            if (!newPassword.equals(confirmPassword)) {
                redirectAttributes.addFlashAttribute("error", "两次输入的新密码不一致");
                return "redirect:/account/profile";
            }

            fileStorageService.changePassword(userId, oldPassword, newPassword);
            redirectAttributes.addFlashAttribute("message", "密码修改成功");
            
        } catch (IllegalArgumentException e) {
            log.warn("密码修改参数错误: {}", e.getMessage());
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        } catch (Exception e) {
            log.error("密码修改失败", e);
            redirectAttributes.addFlashAttribute("error", "密码修改失败，请稍后重试");
        }

        return "redirect:/account/profile";
    }

    /**
     * 修改邮箱
     */
    @PostMapping("/change-email")
    public String changeEmail(@RequestParam String email,
                              Authentication authentication,
                              RedirectAttributes redirectAttributes) {
        try {
            Long userId = getCurrentUserId(authentication);
            if (userId == null) {
                redirectAttributes.addFlashAttribute("error", "用户未登录");
                return "redirect:/login";
            }

            fileStorageService.changeEmail(userId, email);
            redirectAttributes.addFlashAttribute("message", "邮箱修改成功");
            
        } catch (IllegalArgumentException e) {
            log.warn("邮箱修改参数错误: {}", e.getMessage());
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        } catch (Exception e) {
            log.error("邮箱修改失败", e);
            redirectAttributes.addFlashAttribute("error", "邮箱修改失败，请稍后重试");
        }

        return "redirect:/account/profile";
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
