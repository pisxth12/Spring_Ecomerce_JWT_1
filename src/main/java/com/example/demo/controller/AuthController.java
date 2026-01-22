package com.example.demo.controller;

import com.example.demo.dto.request.LoginRequest;
import com.example.demo.dto.request.RegisterRequest;
import com.example.demo.dto.response.ApiResponse;
import com.example.demo.dto.response.AuthResponse;
import com.example.demo.security.JwtTokenProvider;
import com.example.demo.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;


import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {
   private final AuthenticationManager authenticationManage;
   private final JwtTokenProvider tokenProvider;
   private final UserService userService;

   @PostMapping("/login")
    public ResponseEntity<ApiResponse> login(@Valid @RequestBody LoginRequest request){
       Authentication authentication = authenticationManage.authenticate(
               new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
       );
       SecurityContextHolder.getContext().setAuthentication(authentication);
       String jwt = tokenProvider.generateToken(authentication);
       AuthResponse authResponse = new AuthResponse(
               request.getEmail(),
               authentication.getAuthorities().iterator().next().getAuthority(),jwt
       );
       return ResponseEntity.ok(new ApiResponse(true, "Login successfully", authResponse));
   }

   @PostMapping("/register")
    public ResponseEntity<ApiResponse> register(@Valid @RequestBody RegisterRequest request){
       userService.registerUser(request);
       return ResponseEntity.ok(new ApiResponse(true, "Register successfully", null));
   }

   @GetMapping("/me")
    public ResponseEntity<ApiResponse> getCurrentUser(){
       Authentication auth = SecurityContextHolder.getContext().getAuthentication();
       return ResponseEntity.ok(new ApiResponse(true, "Current user", auth.getName()));
   }

}
