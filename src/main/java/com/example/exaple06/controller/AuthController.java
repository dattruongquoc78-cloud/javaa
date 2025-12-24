package com.example.exaple06.controller;

import com.example.exaple06.dto.*;
import com.example.exaple06.entity.User;
import com.example.exaple06.enums.Role;
import com.example.exaple06.repository.UserRepository;
import com.example.exaple06.config.JwtUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class AuthController {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtUtils jwtUtils;

    @PostMapping("/register")
    public ResponseEntity<ApiResponse> register(@RequestBody RegisterRequest request) {
        try {
            if (userRepository.findByUsername(request.getUsername()).isPresent()) {
                return ResponseEntity.badRequest().body(ApiResponse.error("❌ Username đã tồn tại"));
            }

            if (userRepository.existsByEmail(request.getEmail())) {
                return ResponseEntity.badRequest().body(ApiResponse.error("❌ Email đã tồn tại"));
            }

            User user = new User();
            user.setUsername(request.getUsername());
            user.setPassword(passwordEncoder.encode(request.getPassword()));
            user.setEmail(request.getEmail());
            user.setFullName(request.getFullName());
            user.setPhone(request.getPhone());
            
            // ✅ FIX: LUÔN là CUSTOMER khi đăng ký (bỏ qua role từ request)
            user.setRole(Role.CUSTOMER);
            user.setIsActive(true);

            userRepository.save(user);

            return ResponseEntity.status(201).body(ApiResponse.success("✅ Đăng ký thành công"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error("❌ Lỗi đăng ký: " + e.getMessage()));
        }
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse> login(@RequestBody LoginRequest request) {
        try {
            System.out.println("=== DEBUG LOGIN START ===");
            System.out.println("Username: " + request.getUsername());
            System.out.println("Password: " + request.getPassword());
            
            // ✅ Thử authenticate
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword())
            );

            System.out.println("=== DEBUG: Authentication SUCCESS ===");
            
            SecurityContextHolder.getContext().setAuthentication(authentication);
            String token = jwtUtils.generateToken(authentication);

            User user = userRepository.findByUsername(request.getUsername()).orElseThrow();
            
            List<String> roles = List.of(user.getRole().name());

            JwtResponse response = new JwtResponse(
                token, 
                user.getId(), 
                user.getUsername(), 
                user.getEmail(), 
                roles
            );

            System.out.println("=== DEBUG: Login COMPLETE ===");
            return ResponseEntity.ok(ApiResponse.success("✅ Đăng nhập thành công", response));
        } catch (Exception e) {
            System.out.println("=== DEBUG: Login FAILED ===");
            System.out.println("Error: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.badRequest().body(ApiResponse.error("❌ Sai username hoặc password"));
        }
    }
}