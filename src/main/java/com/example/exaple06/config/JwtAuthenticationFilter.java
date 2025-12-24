package com.example.exaple06.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtUtils jwtUtils;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        try {
            String authHeader = request.getHeader("Authorization");

            if (authHeader != null && authHeader.startsWith("Bearer ")) {
                String token = authHeader.substring(7);

                if (jwtUtils.validateToken(token)) {
                    String username = jwtUtils.getUsernameFromToken(token);
                    String role = jwtUtils.extractRole(token);

                    // 🧩 Đảm bảo role có prefix ROLE_
                    if (role != null && !role.startsWith("ROLE_")) {
                        role = "ROLE_" + role;
                    }

                    if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                        var authority = new SimpleGrantedAuthority(role);
                        var authentication = new org.springframework.security.authentication.UsernamePasswordAuthenticationToken(
                                username, null, List.of(authority)
                        );

                        authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                        SecurityContextHolder.getContext().setAuthentication(authentication);

                        // 🪪 Debug log giúp bạn xác nhận token hợp lệ
                        System.out.println("✅ [JWT Filter] Authenticated user: " + username + " | Role: " + role);
                    }
                } else {
                    System.out.println("⚠️ [JWT Filter] Token không hợp lệ cho request: " + request.getRequestURI());
                }
            } else {
                // Không có header Authorization
                System.out.println("⚠️ [JWT Filter] No Bearer token for request: " + request.getRequestURI());
            }

        } catch (Exception e) {
            System.err.println("❌ [JWT Filter error] " + e.getMessage());
        }

        filterChain.doFilter(request, response);
    }
}
