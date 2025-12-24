package com.example.exaple06.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .csrf(csrf -> csrf.disable())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        // Public Auth
                        .requestMatchers("/api/auth/**").permitAll()
                        .requestMatchers("/api/home/**").permitAll()

                        // ---- 🔥 PAYOS webhook cần mở hoàn toàn ----
                        .requestMatchers("/api/payments/payos/callback").permitAll()
                        .requestMatchers("/api/payments/payos/**").permitAll()
                        // --------------------------------------------

                        // Menu public
                        .requestMatchers(HttpMethod.GET,
                                "/api/products/**",
                                "/api/categories/**",
                                "/api/tables/**")
                        .permitAll()

                        // Order public (cho khách)
                        .requestMatchers(HttpMethod.POST, "/api/orders/**").permitAll()

                        // WebSocket no auth
                        .requestMatchers("/ws/**").permitAll()

                        // Employee + admin
                        .requestMatchers(HttpMethod.GET, "/api/orders/**")
                        .hasAnyAuthority("ROLE_EMPLOYEE", "ROLE_ADMIN")

                        .requestMatchers(HttpMethod.PUT, "/api/orders/*/status")
                        .hasAnyAuthority("ROLE_EMPLOYEE", "ROLE_ADMIN")

                        // Reports + internal payments API → require role
                        .requestMatchers("/api/payments/**")
                        .hasAnyAuthority("ROLE_EMPLOYEE", "ROLE_ADMIN")

                        .requestMatchers("/api/report/**")
                        .hasAnyAuthority("ROLE_EMPLOYEE", "ROLE_ADMIN")

                        // Products & categories CRUD
                        .requestMatchers(HttpMethod.POST, "/api/products/**", "/api/categories/**")
                        .hasAnyAuthority("ROLE_EMPLOYEE", "ROLE_ADMIN")

                        .requestMatchers(HttpMethod.PUT, "/api/products/**", "/api/categories/**")
                        .hasAnyAuthority("ROLE_EMPLOYEE", "ROLE_ADMIN")

                        .requestMatchers(HttpMethod.DELETE, "/api/products/**", "/api/categories/**")
                        .hasAnyAuthority("ROLE_EMPLOYEE", "ROLE_ADMIN")

                        // Uploads
                        .requestMatchers("/api/upload").permitAll()
                        .requestMatchers("/uploads/**").permitAll()

                        // Admin-only
                        .requestMatchers("/api/users/**", "/api/bills/**", "/api/promotions/**")
                        .hasAuthority("ROLE_ADMIN")

                        // Default allow (prevent 403 to FE)
                        .anyRequest().permitAll())

                // ✅ Thêm filter xác thực JWT
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        // ⚠️ Dùng cho dev/test — khi deploy thật nên dùng BCrypt
        return org.springframework.security.crypto.password.NoOpPasswordEncoder.getInstance();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authConfig) throws Exception {
        return authConfig.getAuthenticationManager();
    }

    // ✅ Cấu hình CORS để fix lỗi “Access-Control-Allow-Origin”
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(List.of("http://localhost:3000")); // chỉ cho phép FE
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(List.of("*"));
        configuration.setAllowCredentials(true); // bắt buộc khi gửi token/cookie

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}
