package com.example.exaple06.dto;

import com.example.exaple06.enums.Role;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RegisterRequest {
    private String username;
    private String password;
    private String email;
    private String fullName;
    private String phone;
    private Role role = Role.EMPLOYEE; // ✅ Mặc định là EMPLOYEE
}