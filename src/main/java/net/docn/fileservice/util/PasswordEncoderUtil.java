package net.docn.fileservice.util;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

/**
 * 密码加密工具类
 * 用于生成 BCrypt 加密后的密码
 */
public class PasswordEncoderUtil {

    public static void main(String[] args) {
        if (args.length == 0) {
            System.out.println("使用方法:");
            System.out.println("1. 直接运行（使用默认密码 admin123）:");
            System.out.println("   java PasswordEncoderUtil");
            System.out.println();
            System.out.println("2. 指定密码:");
            System.out.println("   java PasswordEncoderUtil your_password");
            System.out.println();
            
            // 默认示例
            String defaultPassword = "admin123";
            String encoded = encodePassword(defaultPassword);
            System.out.println("========== 示例 ==========");
            System.out.println("明文密码: " + defaultPassword);
            System.out.println("加密后: " + encoded);
            System.out.println("=========================");
        } else {
            String password = args[0];
            String encoded = encodePassword(password);
            System.out.println("明文密码: " + password);
            System.out.println("加密后: " + encoded);
            System.out.println();
            System.out.println("SQL 更新语句:");
            System.out.println("UPDATE users SET password = '" + encoded + "' WHERE username = 'admin';");
        }
    }

    /**
     * 使用 BCrypt 加密密码
     * @param plainPassword 明文密码
     * @return 加密后的密码
     */
    public static String encodePassword(String plainPassword) {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        return encoder.encode(plainPassword);
    }
}
