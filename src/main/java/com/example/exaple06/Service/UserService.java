package com.example.exaple06.Service;

import com.example.exaple06.entity.User;
import com.example.exaple06.enums.Role;
import com.example.exaple06.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserService {
    
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public List<User> getAll() {
        return userRepository.findAll();
    }

    public Optional<User> getById(Long id) {
        return userRepository.findById(id);
    }

    public User save(User user) {
        // ✅ Mã hóa password trước khi lưu
        if (user.getPassword() != null && !user.getPassword().startsWith("$2a$")) {
            user.setPassword(passwordEncoder.encode(user.getPassword()));
        }
        return userRepository.save(user);
    }

    public void delete(Long id) {
        userRepository.deleteById(id);
    }

    // ✅ THÊM: Tìm user theo username
    public Optional<User> findByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    // ✅ THÊM: Tìm user theo role
    public List<User> findByRole(Role role) {
        return userRepository.findByRole(role);
    }

    // ✅ THÊM: Kiểm tra username tồn tại
    public boolean existsByUsername(String username) {
        return userRepository.existsByUsername(username);
    }

    // ✅ THÊM: Kiểm tra email tồn tại
    public boolean existsByEmail(String email) {
        return userRepository.existsByEmail(email);
    }
}