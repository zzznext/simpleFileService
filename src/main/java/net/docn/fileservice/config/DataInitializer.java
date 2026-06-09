//package net.docn.fileservice.config;
//
//import net.docn.fileservice.entity.User;
//import net.docn.fileservice.repository.UserRepository;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.CommandLineRunner;
//import org.springframework.security.crypto.password.PasswordEncoder;
//import org.springframework.stereotype.Component;
//
//import java.time.LocalDateTime;
//
//@Component
//public class DataInitializer implements CommandLineRunner {
//
//    @Autowired
//    private UserRepository userRepository;
//
//    @Autowired
//    private PasswordEncoder passwordEncoder;
//
//    @Override
//    public void run(String... args) throws Exception {
//        // 检查是否已有admin用户
//        if (userRepository.findByUsername("admin").isEmpty()) {
//            User admin = new User();
//            admin.setUsername("admin");
//            admin.setPassword(passwordEncoder.encode("admin123"));
//            admin.setEmail("admin@example.com");
//            admin.setCreatedAt(LocalDateTime.now());
//            admin.setUpdatedAt(LocalDateTime.now());
//
//            userRepository.save(admin);
//            System.out.println("默认管理员账户已创建: admin / admin123");
//        } else {
//            System.out.println("管理员账户已存在，跳过创建");
//        }
//    }
//}
