package net.docn.fileservice.service;

import org.springframework.stereotype.Service;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

@Service
public class CaptchaService {
    
    private static final int WIDTH = 120;
    private static final int HEIGHT = 40;
    private static final int CODE_LENGTH = 4;
    private static final String CHARACTERS = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
    
    /**
     * 生成验证码图片和文本
     * @return 包含图片和验证码文本的Map
     */
    public Map<String, Object> generateCaptcha() {
        // 创建图片
        BufferedImage image = new BufferedImage(WIDTH, HEIGHT, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2d = image.createGraphics();
        
        // 设置背景色
        g2d.setColor(Color.WHITE);
        g2d.fillRect(0, 0, WIDTH, HEIGHT);
        
        // 设置字体
        Font font = new Font("Arial", Font.BOLD, 24);
        g2d.setFont(font);
        
        // 生成随机验证码
        Random random = new Random();
        StringBuilder code = new StringBuilder();
        
        for (int i = 0; i < CODE_LENGTH; i++) {
            String charStr = String.valueOf(CHARACTERS.charAt(random.nextInt(CHARACTERS.length())));
            code.append(charStr);
            
            // 随机颜色
            g2d.setColor(new Color(
                random.nextInt(100),
                random.nextInt(100),
                random.nextInt(100)
            ));
            
            // 绘制字符，带随机旋转
            int x = 20 + i * 25;
            int y = 28;
            double angle = (random.nextDouble() - 0.5) * 0.4;
            
            g2d.rotate(angle, x, y);
            g2d.drawString(charStr, x, y);
            g2d.rotate(-angle, x, y);
        }
        
        // 添加干扰线
        for (int i = 0; i < 5; i++) {
            g2d.setColor(new Color(
                random.nextInt(200),
                random.nextInt(200),
                random.nextInt(200)
            ));
            int x1 = random.nextInt(WIDTH);
            int y1 = random.nextInt(HEIGHT);
            int x2 = random.nextInt(WIDTH);
            int y2 = random.nextInt(HEIGHT);
            g2d.drawLine(x1, y1, x2, y2);
        }
        
        // 添加干扰点
        for (int i = 0; i < 30; i++) {
            g2d.setColor(new Color(
                random.nextInt(200),
                random.nextInt(200),
                random.nextInt(200)
            ));
            int x = random.nextInt(WIDTH);
            int y = random.nextInt(HEIGHT);
            g2d.fillOval(x, y, 2, 2);
        }
        
        g2d.dispose();
        
        Map<String, Object> result = new HashMap<>();
        result.put("image", image);
        result.put("code", code.toString().toLowerCase());
        
        return result;
    }
}
