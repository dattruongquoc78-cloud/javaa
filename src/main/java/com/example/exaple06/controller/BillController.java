package com.example.exaple06.controller;

import com.example.exaple06.dto.BillRequest;
import com.example.exaple06.dto.ApiResponse;
import com.example.exaple06.entity.Bill;
import com.example.exaple06.Service.BillService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/bills")
@CrossOrigin(origins = "http://localhost:3000")
@RequiredArgsConstructor
public class BillController {

    private final BillService billService;

    @GetMapping
    public ResponseEntity<ApiResponse> getAllBills() {
        try {
            List<Bill> bills = billService.getAll();
            return ResponseEntity.ok(ApiResponse.success("✅ Lấy danh sách hóa đơn thành công", bills));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error("❌ Lỗi: " + e.getMessage()));
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse> getBillById(@PathVariable Long id) {
        try {
            return billService.getById(id)
                    .map(bill -> ResponseEntity.ok(ApiResponse.success("✅ Lấy hóa đơn thành công", bill)))
                    .orElse(ResponseEntity.notFound().build());
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error("❌ Lỗi: " + e.getMessage()));
        }
    }

    @PostMapping
    public ResponseEntity<ApiResponse> createBill(@RequestBody BillRequest request) {
        try {
            Bill saved = billService.createBill(request);
            return ResponseEntity.status(201).body(ApiResponse.success("✅ Tạo hóa đơn thành công", saved));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error("❌ Lỗi tạo hóa đơn: " + e.getMessage()));
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse> deleteBill(@PathVariable Long id) {
        try {
            billService.delete(id);
            return ResponseEntity.ok(ApiResponse.success("✅ Xóa hóa đơn thành công"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error("❌ Lỗi xóa: " + e.getMessage()));
        }
    }
}