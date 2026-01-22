package com.example.demo.service;

import com.example.demo.dto.request.RegisterRequest;
import com.example.demo.entity.User;
import org.springframework.stereotype.Service;

@Service
public interface UserService {
    User registerUser(RegisterRequest request);
    User getUserByEmail(String email);
}
