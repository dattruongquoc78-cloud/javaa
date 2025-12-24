package com.example.exaple06.entity;

import com.example.exaple06.enums.Role;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp; // ✅ THÊM DÒNG NÀY
import org.hibernate.annotations.UpdateTimestamp;   // ✅ THÊM DÒNG NÀY
import java.time.LocalDateTime;                    // ✅ THÊM DÒNG NÀY

@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String username;
    private String email;
    private String password;

    @Enumerated(EnumType.STRING)
    private Role role;

    private String fullName;
    private String phone;
    private String address;
    
    // ✅ THÊM CÁC TRƯỜNG NÀY
    private String imageUrl; // Theo thiết kế CSDL
    private Boolean isActive = true; // Theo thiết kế CSDL
    
    @CreationTimestamp
    private LocalDateTime createdAt;
    
    @UpdateTimestamp
    private LocalDateTime updatedAt;
}
