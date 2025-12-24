package com.example.exaple06.controller;

import com.example.exaple06.Service.HomeService; // ✅ Service (S hoa)
import com.example.exaple06.dto.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/home")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class HomeController {

    private final HomeService homeService; // ✅ KHỚP VỚI IMPORT

    @GetMapping
    public ResponseEntity<ApiResponse> getHomeData() {
        try {
            Object homeData = homeService.getHomeData();
            return ResponseEntity.ok(ApiResponse.success("✅ Lấy dữ liệu trang chủ thành công", homeData));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error("❌ Lỗi: " + e.getMessage()));
        }
    }
}