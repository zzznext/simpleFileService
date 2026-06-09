package net.docn.fileservice.config;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * 验证码过滤器 - 在登录时验证验证码
 */
@Component
public class CaptchaFilter implements Filter {
    
    private static final Logger logger = LoggerFactory.getLogger(CaptchaFilter.class);
    
    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;
        
        // 只拦截登录请求
        String requestURI = httpRequest.getRequestURI();
        if (requestURI != null && requestURI.endsWith("/login") && "POST".equalsIgnoreCase(httpRequest.getMethod())) {
            HttpSession session = httpRequest.getSession(false); // 不创建新session
            
            // 获取用户输入的验证码
            String inputCaptcha = httpRequest.getParameter("captcha");
            logger.info("用户输入的验证码: {}", inputCaptcha);
            
            // 获取session中存储的正确验证码
            String correctCaptcha = session != null ? (String) session.getAttribute("captchaCode") : null;

            // 验证验证码
            if (inputCaptcha == null || inputCaptcha.trim().isEmpty()) {
                // 验证码为空
                logger.warn("验证码为空");
                httpResponse.sendRedirect("/login?error=captcha_empty");
                return;
            }
            
            if (correctCaptcha == null || correctCaptcha.trim().isEmpty()) {
                // 验证码已过期或未生成
                logger.warn("验证码不存在或已过期");
                httpResponse.sendRedirect("/login?error=captcha_expired");
                return;
            }
            
            if (!inputCaptcha.trim().equalsIgnoreCase(correctCaptcha)) {
                // 验证码错误
                logger.warn("验证码错误 - 输入: {}, 正确: {}", inputCaptcha, correctCaptcha);
                httpResponse.sendRedirect("/login?error=captcha_wrong");
                return;
            }
            
            logger.info("验证码验证通过");
            // 验证通过后，清除session中的验证码（防止重复使用）
            session.removeAttribute("captchaCode");
        }
        
        chain.doFilter(request, response);
    }
}
