package com.example.exaple06.controller;

import com.example.exaple06.Service.TableService;
import com.example.exaple06.dto.ApiResponse;
import com.example.exaple06.entity.TableEntity;
import com.example.exaple06.enums.TableStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/tables")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class TableController {

    private final TableService tableService;

    @GetMapping
    public ResponseEntity<ApiResponse> getAllTables() {
        try {
            List<TableEntity> tables = tableService.getAllTables();
            return ResponseEntity.ok(ApiResponse.success("✅ Lấy danh sách bàn thành công", tables));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error("❌ Lỗi: " + e.getMessage()));
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse> getTableById(@PathVariable Long id) {
        try {
            return tableService.getTableById(id)
                    .map(table -> ResponseEntity.ok(ApiResponse.success("✅ Lấy thông tin bàn thành công", table)))
                    .orElse(ResponseEntity.notFound().build());
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error("❌ Lỗi: " + e.getMessage()));
        }
    }

    @PostMapping
    public ResponseEntity<ApiResponse> createTable(@RequestBody TableEntity table) {
        try {
            TableEntity saved = tableService.createTable(table);
            return ResponseEntity.status(201).body(ApiResponse.success("✅ Tạo bàn thành công", saved));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error("❌ Lỗi tạo bàn: " + e.getMessage()));
        }
    }

    @PutMapping("/{id}/status")
    public ResponseEntity<ApiResponse> updateTableStatus(@PathVariable Long id, @RequestParam TableStatus status) {
        try {
            TableEntity table = tableService.updateTableStatus(id, status);
            return ResponseEntity.ok(ApiResponse.success("✅ Cập nhật trạng thái bàn thành công", table));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error("❌ Lỗi: " + e.getMessage()));
        }
    }

    @GetMapping("/status/{status}")
    public ResponseEntity<ApiResponse> getTablesByStatus(@PathVariable TableStatus status) {
        try {
            List<TableEntity> tables = tableService.getTablesByStatus(status);
            return ResponseEntity.ok(ApiResponse.success("✅ Lấy bàn theo trạng thái thành công", tables));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error("❌ Lỗi: " + e.getMessage()));
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse> deleteTable(@PathVariable Long id) {
        try {
            tableService.deleteTable(id);
            return ResponseEntity.ok(ApiResponse.success("✅ Xóa bàn thành công"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error("❌ Lỗi xóa: " + e.getMessage()));
        }
    }
}