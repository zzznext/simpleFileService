package net.docn.fileservice.controller;

import net.docn.fileservice.service.CustomUserDetails;
import net.docn.fileservice.service.SystemConfigService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Map;

/**
 * 系统设置控制器
 */
@Controller
@RequestMapping("/admin/settings")
public class SystemSettingsController {
    
    private static final Logger log = LoggerFactory.getLogger(SystemSettingsController.class);
    
    @Autowired
    private SystemConfigService configService;
    
    /**
     * 系统设置主页（重定向到存储配置）
     */
    @GetMapping
    public String settingsPage() {
        return "redirect:/admin/settings/storage";
    }
    
    /**
     * 存储配置页面
     */
    @GetMapping("/storage")
    public String storageSettings(Model model, Authentication authentication) {
        // 权限检查
        if (!isAdmin(authentication)) {
            return "redirect:/files";
        }
        
        // 获取存储配置
        Map<String, String> configs = configService.getConfigsByGroup("STORAGE");
        model.addAttribute("configs", configs);
        model.addAttribute("activeTab", "storage");
        
        return "admin/settings-storage";
    }
    
    /**
     * 保存存储配置
     */
    @PostMapping("/storage")
    public String saveStorageSettings(@RequestParam Map<String, String> configs,
                                     RedirectAttributes redirectAttributes) {
        try {
            configService.batchUpdateConfigs(configs);
            redirectAttributes.addFlashAttribute("message", "存储配置保存成功");
            log.info("存储配置已更新");
        } catch (Exception e) {
            log.error("保存存储配置失败", e);
            redirectAttributes.addFlashAttribute("error", "保存失败: " + e.getMessage());
        }
        return "redirect:/admin/settings/storage";
    }
    
    /**
     * 文件类型配置页面
     */
    @GetMapping("/file")
    public String fileSettings(Model model, Authentication authentication) {
        if (!isAdmin(authentication)) {
            return "redirect:/files";
        }
        
        Map<String, String> configs = configService.getConfigsByGroup("FILE");
        model.addAttribute("configs", configs);
        model.addAttribute("activeTab", "file");
        
        return "admin/settings-file";
    }
    
    /**
     * 保存文件类型配置
     */
    @PostMapping("/file")
    public String saveFileSettings(@RequestParam Map<String, String> configs,
                                  RedirectAttributes redirectAttributes) {
        try {
            configService.batchUpdateConfigs(configs);
            redirectAttributes.addFlashAttribute("message", "文件类型配置保存成功");
            log.info("文件类型配置已更新");
        } catch (Exception e) {
            log.error("保存文件类型配置失败", e);
            redirectAttributes.addFlashAttribute("error", "保存失败: " + e.getMessage());
        }
        return "redirect:/admin/settings/file";
    }
    
    /**
     * 安全策略配置页面
     */
    @GetMapping("/security")
    public String securitySettings(Model model, Authentication authentication) {
        if (!isAdmin(authentication)) {
            return "redirect:/files";
        }
        
        Map<String, String> configs = configService.getConfigsByGroup("SECURITY");
        model.addAttribute("configs", configs);
        model.addAttribute("activeTab", "security");
        
        return "admin/settings-security";
    }
    
    /**
     * 保存安全策略配置
     */
    @PostMapping("/security")
    public String saveSecuritySettings(@RequestParam Map<String, String> configs,
                                      RedirectAttributes redirectAttributes) {
        try {
            configService.batchUpdateConfigs(configs);
            redirectAttributes.addFlashAttribute("message", "安全策略配置保存成功");
            log.info("安全策略配置已更新");
        } catch (Exception e) {
            log.error("保存安全策略配置失败", e);
            redirectAttributes.addFlashAttribute("error", "保存失败: " + e.getMessage());
        }
        return "redirect:/admin/settings/security";
    }
    
    /**
     * 系统配置页面
     */
    @GetMapping("/system")
    public String systemSettings(Model model, Authentication authentication) {
        if (!isAdmin(authentication)) {
            return "redirect:/files";
        }
        
        Map<String, String> configs = configService.getConfigsByGroup("SYSTEM");
        model.addAttribute("configs", configs);
        model.addAttribute("activeTab", "system");
        
        return "admin/settings-system";
    }
    
    /**
     * 保存系统配置
     */
    @PostMapping("/system")
    public String saveSystemSettings(@RequestParam Map<String, String> configs,
                                    RedirectAttributes redirectAttributes) {
        try {
            configService.batchUpdateConfigs(configs);
            redirectAttributes.addFlashAttribute("message", "系统配置保存成功");
            log.info("系统配置已更新");
        } catch (Exception e) {
            log.error("保存系统配置失败", e);
            redirectAttributes.addFlashAttribute("error", "保存失败: " + e.getMessage());
        }
        return "redirect:/admin/settings/system";
    }
    
    /**
     * 判断是否为管理员
     */
    private boolean isAdmin(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return false;
        }
        
        Object principal = authentication.getPrincipal();
        if (principal instanceof CustomUserDetails) {
            return ((CustomUserDetails) principal).isAdmin();
        }
        
        return false;
    }
}
