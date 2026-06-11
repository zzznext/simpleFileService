package net.docn.fileservice.config;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import net.docn.fileservice.service.CustomUserDetails;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.SavedRequestAwareAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * 自定义认证成功处理器
 * 根据用户角色跳转到不同的页面
 */
@Component
public class CustomAuthenticationSuccessHandler extends SavedRequestAwareAuthenticationSuccessHandler {

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request,
                                        HttpServletResponse response,
                                        Authentication authentication) throws IOException, ServletException {
        
        // 获取用户详情
        Object principal = authentication.getPrincipal();
        
        if (principal instanceof CustomUserDetails) {
            CustomUserDetails userDetails = (CustomUserDetails) principal;
            
            // 根据角色决定跳转路径
            if (userDetails.isAdmin()) {
                // 管理员跳转到控制台
                getRedirectStrategy().sendRedirect(request, response, "/admin/dashboard");
            } else {
                // 普通用户跳转到文件列表
                super.onAuthenticationSuccess(request, response, authentication);
            }
        } else {
            // 其他情况使用默认行为
            super.onAuthenticationSuccess(request, response, authentication);
        }
    }
}
