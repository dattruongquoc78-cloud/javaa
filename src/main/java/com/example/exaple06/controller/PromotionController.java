package com.example.exaple06.controller;

import com.example.exaple06.Service.PromotionService;
import com.example.exaple06.dto.ApiResponse;
import com.example.exaple06.entity.Promotion;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/promotions")
@CrossOrigin(origins = "http://localhost:3000")
@RequiredArgsConstructor
public class PromotionController {

    private final PromotionService promotionService;

    @GetMapping
    public ResponseEntity<ApiResponse> getAllPromotions() {
        try {
            List<Promotion> promotions = promotionService.getAllPromotions();
            return ResponseEntity.ok(ApiResponse.success("✅ Lấy danh sách khuyến mãi thành công", promotions));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error("❌ Lỗi: " + e.getMessage()));
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse> getPromotionById(@PathVariable Long id) {
        try {
            return promotionService.getPromotionById(id)
                    .map(promotion -> ResponseEntity.ok(ApiResponse.success("✅ Lấy khuyến mãi thành công", promotion)))
                    .orElse(ResponseEntity.notFound().build());
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error("❌ Lỗi: " + e.getMessage()));
        }
    }

    @PostMapping
    public ResponseEntity<ApiResponse> createPromotion(@RequestBody Promotion promotion) {
        try {
            Promotion saved = promotionService.createPromotion(promotion);
            return ResponseEntity.status(201).body(ApiResponse.success("✅ Tạo khuyến mãi thành công", saved));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error("❌ Lỗi tạo: " + e.getMessage()));
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse> updatePromotion(@PathVariable Long id, @RequestBody Promotion promotion) {
        try {
            Promotion updated = promotionService.updatePromotion(id, promotion);
            return ResponseEntity.ok(ApiResponse.success("✅ Cập nhật khuyến mãi thành công", updated));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error("❌ Lỗi cập nhật: " + e.getMessage()));
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse> deletePromotion(@PathVariable Long id) {
        try {
            promotionService.deletePromotion(id);
            return ResponseEntity.ok(ApiResponse.success("✅ Xóa khuyến mãi thành công"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error("❌ Lỗi xóa: " + e.getMessage()));
        }
    }

    @GetMapping("/active")
    public ResponseEntity<ApiResponse> getActivePromotions() {
        try {
            List<Promotion> promotions = promotionService.getActivePromotions();
            return ResponseEntity.ok(ApiResponse.success("✅ Lấy khuyến mãi đang hoạt động thành công", promotions));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error("❌ Lỗi: " + e.getMessage()));
        }
    }

    // ✅ Áp dụng mã khuyến mãi
    @PostMapping("/validate")
    @PreAuthorize("hasAnyAuthority('ROLE_EMPLOYEE','ROLE_ADMIN')")
    public ResponseEntity<ApiResponse> validatePromotion(@RequestBody Map<String, String> req) {
        String code = req.getOrDefault("code", "").trim();
        if (code.isEmpty()) {
            return ResponseEntity.badRequest().body(ApiResponse.error("❌ Vui lòng nhập mã khuyến mãi"));
        }

        Optional<Promotion> ok = promotionService.validateAndUsePromotion(code);
        return ok.<ResponseEntity<ApiResponse>>map(p ->
                ResponseEntity.ok(ApiResponse.success("✅ Áp dụng khuyến mãi thành công", p))
        ).orElseGet(() ->
                ResponseEntity.ok(ApiResponse.error("❌ Mã không hợp lệ / đã hết hạn / hết lượt sử dụng"))
        );
    }

    // ✅ Lấy danh sách khuyến mãi sắp hết hạn
    @GetMapping("/almost-expired")
    @PreAuthorize("hasAnyAuthority('ROLE_EMPLOYEE','ROLE_ADMIN')")
    public ResponseEntity<ApiResponse> almostExpired(@RequestParam(defaultValue = "3") int days) {
        List<Promotion> promotions = promotionService.getAlmostExpired(days);
        return ResponseEntity.ok(ApiResponse.success("✅ Danh sách khuyến mãi sắp hết hạn", promotions));
    }
}
