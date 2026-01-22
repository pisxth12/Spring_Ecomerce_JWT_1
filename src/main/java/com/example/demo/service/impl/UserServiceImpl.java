package com.example.demo.service.impl;

import com.example.demo.dto.request.RegisterRequest;
import com.example.demo.entity.User;
import com.example.demo.repository.UserRepository;
import com.example.demo.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public User registerUser(RegisterRequest request){
        //Check if user already exists
        if(userRepository.existsByEmail(request.getEmail())){
            throw new RuntimeException("Email already  exists");
        }
        User user = new User();
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));

        if(request.getRole() == null || request.getRole().isEmpty()){
            user.setRole(User.Role.ROLE_USER);
        }else{
            try{
                user.setRole(User.Role.valueOf(request.getRole().toLowerCase()));
            }catch (IllegalArgumentException  e){
                user.setRole(User.Role.ROLE_USER);
            }
        }
        return userRepository.save(user);
    }

    @Override
    public User getUserByEmail(String email){
        return userRepository.findByEmail(email).orElseThrow(()-> new RuntimeException("User not found"));
    }

}
