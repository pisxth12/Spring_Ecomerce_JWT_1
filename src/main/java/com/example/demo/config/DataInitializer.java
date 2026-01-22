//package com.example.demo.config;
//
//import com.example.demo.entity.User;
//import com.example.demo.repository.UserRepository;
//import lombok.RequiredArgsConstructor;
//import org.springframework.boot.CommandLineRunner;
//import org.springframework.security.crypto.password.PasswordEncoder;
//import org.springframework.stereotype.Component;
//
//@Component
//@RequiredArgsConstructor
//public class DataInitializer implements CommandLineRunner {
//    private final UserRepository userRepository;
//    private final PasswordEncoder passwordEncoder;
//
//    @Override
//    public void run(String... args){
//        // Create admin user
//        if(!userRepository.existsByEmail("admin6@example.com")){
//            User admin = new User();
//            admin.setEmail("admin6@example.com");
//            admin.setPassword(passwordEncoder.encode("admin123"));
//            admin.setRole(User.Role.ROLE_ADMIN);
//            userRepository.save(admin);
//            System.out.println("Admin created: admin@example.com / admin123");
//        }
//
//        // Create regular user
//        if(!userRepository.existsByEmail("user6@example.com")){
//            User user = new User();
//            user.setEmail("user6@wxample.com");
//            user.setPassword(passwordEncoder.encode("user123"));
//            user.setRole(User.Role.ROLE_USER);
//            userRepository.save(user);
//            System.out.println("User created: user@example.com / user123");
//        }
//    }
//
//
//
//}
