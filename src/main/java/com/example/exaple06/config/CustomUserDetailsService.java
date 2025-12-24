package com.example.exaple06.config;

import com.example.exaple06.entity.User;
import com.example.exaple06.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        System.out.println("=== DEBUG: Searching for user: " + username + " ===");
        
        // ✅ Tìm user trong database
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> {
                    System.out.println("=== DEBUG: User NOT FOUND: " + username + " ===");
                    return new UsernameNotFoundException("User not found: " + username);
                });

        // ✅ Debug chi tiết
        System.out.println("=== DEBUG USER INFO ===");
        System.out.println("ID: " + user.getId());
        System.out.println("Username: " + user.getUsername());
        System.out.println("Password: " + user.getPassword());
        System.out.println("Password length: " + (user.getPassword() != null ? user.getPassword().length() : "null"));
        System.out.println("Role: " + user.getRole());
        System.out.println("IsActive: " + user.getIsActive());
        System.out.println("=========================");

        // ✅ Kiểm tra active
        if (user.getIsActive() == null || !user.getIsActive()) {
            System.out.println("=== DEBUG: User is INACTIVE ===");
            throw new UsernameNotFoundException("User is deactivated: " + username);
        }

        // ✅ FIX: DÙNG ROLE_ PREFIX THEO CHUẨN SPRING SECURITY
        String role = "ROLE_" + user.getRole().name();
        List<GrantedAuthority> authorities = Collections.singletonList(
            new SimpleGrantedAuthority(role)
        );

        System.out.println("=== DEBUG: Creating UserDetails with role: " + role + " ===");

        return new org.springframework.security.core.userdetails.User(
                user.getUsername(),
                user.getPassword(), // Password từ database
                authorities
        );
    }
}