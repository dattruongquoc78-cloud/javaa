package com.example.exaple06.controller;

import com.example.exaple06.Service.OrderDetailService;
import com.example.exaple06.dto.ApiResponse;
import com.example.exaple06.entity.OrderDetail;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/order-details")
@CrossOrigin(origins = "http://localhost:3000")
@RequiredArgsConstructor
public class OrderDetailController {

    private final OrderDetailService orderDetailService;

    @GetMapping
    public ResponseEntity<ApiResponse> getAll() {
        try {
            List<OrderDetail> details = orderDetailService.getAll();
            return ResponseEntity.ok(ApiResponse.success("✅ Lấy danh sách order details thành công", details));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error("❌ Lỗi: " + e.getMessage()));
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse> getById(@PathVariable Long id) {
        try {
            return orderDetailService.getById(id)
                    .map(detail -> ResponseEntity.ok(ApiResponse.success("✅ Lấy order detail thành công", detail)))
                    .orElse(ResponseEntity.notFound().build());
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error("❌ Lỗi: " + e.getMessage()));
        }
    }

    @PostMapping
    public ResponseEntity<ApiResponse> create(@RequestBody OrderDetail detail) {
        try {
            OrderDetail saved = orderDetailService.save(detail);
            return ResponseEntity.status(201).body(ApiResponse.success("✅ Tạo order detail thành công", saved));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error("❌ Lỗi tạo: " + e.getMessage()));
        }
    }

    @GetMapping("/order/{orderId}")
    public ResponseEntity<ApiResponse> getByOrder(@PathVariable Long orderId) {
        try {
            List<OrderDetail> details = orderDetailService.getByOrderId(orderId);
            return ResponseEntity.ok(ApiResponse.success("✅ Lấy order details theo order thành công", details));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error("❌ Lỗi: " + e.getMessage()));
        }
    }

    @PutMapping("/{id}/quantity")
    public ResponseEntity<ApiResponse> updateQuantity(@PathVariable Long id, @RequestParam Integer quantity) {
        try {
            OrderDetail updated = orderDetailService.updateQuantity(id, quantity);
            return ResponseEntity.ok(ApiResponse.success("✅ Cập nhật số lượng thành công", updated));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error("❌ Lỗi: " + e.getMessage()));
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse> delete(@PathVariable Long id) {
        try {
            orderDetailService.delete(id);
            return ResponseEntity.ok(ApiResponse.success("✅ Xóa order detail thành công"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error("❌ Lỗi xóa: " + e.getMessage()));
        }
    }
}