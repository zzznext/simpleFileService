package net.docn.fileservice.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import net.docn.fileservice.service.CaptchaService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.Map;

@Controller
public class CaptchaController {
    
    private static final Logger logger = LoggerFactory.getLogger(CaptchaController.class);
    
    @Autowired
    private CaptchaService captchaService;
    
    /**
     * 生成验证码图片
     */
    @GetMapping("/captcha")
    public void getCaptcha(HttpServletRequest request, HttpServletResponse response) throws IOException {
        HttpSession session = request.getSession();
        
        // 生成验证码
        Map<String, Object> captcha = captchaService.generateCaptcha();
        BufferedImage image = (BufferedImage) captcha.get("image");
        String code = (String) captcha.get("code");
        
        // 将验证码文本存储到session中
        session.setAttribute("captchaCode", code);

        // 设置响应头，禁止缓存
        session.setAttribute("captchaTime", System.currentTimeMillis());
        
        // 设置响应类型和禁止缓存头
        response.setContentType("image/jpeg");
        response.setHeader("Pragma", "no-cache");
        response.setHeader("Cache-Control", "no-cache");
        response.setDateHeader("Expires", 0);
        
        // 输出图片
        ImageIO.write(image, "JPEG", response.getOutputStream());
    }
}
