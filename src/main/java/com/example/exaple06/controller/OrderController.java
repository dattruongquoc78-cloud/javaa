package com.example.exaple06.controller;

import com.example.exaple06.Service.OrderService;
import com.example.exaple06.dto.OrderRequest;
import com.example.exaple06.entity.Order;
import com.example.exaple06.enums.OrderStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@CrossOrigin(origins = { "http://localhost:3000",
        "http://127.0.0.1:3000" }, allowedHeaders = "*", exposedHeaders = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;
    private final SimpMessagingTemplate messagingTemplate;

    // ✅ Tạo đơn hàng mới
    @PostMapping
    public ResponseEntity<?> createOrder(@RequestBody OrderRequest req) {
        try {
            System.out.println("📦 Received order request: " + req.toString());

            Order saved = orderService.createOrderFromRequest(req);
            System.out.println("✅ Order created successfully: " + saved.getId());

            // 🔔 Gửi đơn hàng mới tới nhân viên
            messagingTemplate.convertAndSend("/topic/orders", saved);

            // 🔔 Gửi cho khách hàng nếu họ có UI theo dõi realtime
            messagingTemplate.convertAndSend("/topic/orders/" + saved.getId(), saved);

            // 🔥 Đồng bộ trạng thái bàn (đổi thành OCCUPIED)
            messagingTemplate.convertAndSend("/topic/tables/status", saved.getTable());

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "✅ Tạo đơn hàng thành công");
            response.put("data", saved);

            return ResponseEntity.status(201).body(response);

        } catch (Exception e) {
            System.err.println("❌ Order creation failed: " + e.getMessage());
            e.printStackTrace();

            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "❌ Lỗi tạo đơn hàng: " + e.getMessage());

            return ResponseEntity.badRequest().body(errorResponse);
        }
    }

    // ✅ Lấy danh sách tất cả đơn hàng
    @GetMapping
    public ResponseEntity<?> getAllOrders() {
        try {
            List<Order> orders = orderService.getAllOrders();

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "✅ Lấy danh sách đơn hàng thành công");
            response.put("data", orders);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "❌ " + e.getMessage());
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }

    // ✅ Lấy đơn hàng theo customerId (MỚI)
    @GetMapping("/customer/{customerId}")
    public ResponseEntity<?> getOrdersByCustomer(@PathVariable Long customerId) {
        try {
            List<Order> orders = orderService.getOrdersByCustomerId(customerId);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "✅ Lấy danh sách đơn hàng thành công");
            response.put("data", orders);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "❌ " + e.getMessage());
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }

    // ✅ Lấy chi tiết 1 đơn hàng theo ID
    @GetMapping("/{id}")
    public ResponseEntity<?> getOrderById(@PathVariable Long id) {
        try {
            Order order = orderService.getOrderById(id)
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy đơn hàng ID = " + id));

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "✅ Lấy chi tiết đơn hàng thành công");
            response.put("data", order);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "❌ " + e.getMessage());
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }

    // ✅ Tìm kiếm đơn hàng theo từ khóa
    @GetMapping("/search")
    public ResponseEntity<?> searchOrders(@RequestParam String keyword) {
        try {
            List<Order> results = orderService.searchOrders(keyword);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "✅ Tìm thấy " + results.size() + " đơn hàng");
            response.put("data", results);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", "❌ Lỗi tìm kiếm: " + e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    // ✅ Cập nhật trạng thái đơn hàng (ĐÃ CẬP NHẬT WEBSOCKET)
    // 🔧 Cập nhật trạng thái đơn hàng
    @PutMapping("/{id}/status")
    public ResponseEntity<?> updateStatus(@PathVariable Long id, @RequestParam OrderStatus status) {
        try {
            Order updated = orderService.updateOrderStatus(id, status);

            // 🔔 Báo realtime cho admin & hệ thống
            messagingTemplate.convertAndSend("/topic/orders/status", updated);
            messagingTemplate.convertAndSend("/topic/orders/" + id, updated);

            // 🔥 Nếu đơn đã thanh toán hoặc hủy → bàn rảnh
            if (updated.getStatus() == OrderStatus.PAID || updated.getStatus() == OrderStatus.CANCELLED) {
                messagingTemplate.convertAndSend("/topic/tables/status", updated.getTable());
            }

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "✅ Cập nhật trạng thái thành công");
            response.put("data", updated);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "❌ Lỗi cập nhật: " + e.getMessage());
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }

    // ✅ Khách yêu cầu thanh toán
    @PutMapping("/{id}/request-payment")
    public ResponseEntity<?> requestPayment(@PathVariable Long id) {
        try {
            Order updated = orderService.updateOrderStatus(id, OrderStatus.REQUEST_PAYMENT);

            // 🔔 Gửi realtime lên màn nhân viên
            messagingTemplate.convertAndSend("/topic/orders/payment", updated);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "✅ Khách đã yêu cầu thanh toán");
            response.put("data", updated);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "❌ " + e.getMessage());
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }

    @PostMapping("/{orderId}/add-item")
    public ResponseEntity<?> addItemToExistingOrder(
            @PathVariable Long orderId,
            @RequestBody OrderRequest req) {
        try {
            Order updatedOrder = orderService.addItemsToOrder(orderId, req);

            messagingTemplate.convertAndSend("/topic/orders", updatedOrder);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Thêm món thành công");
            response.put("data", updatedOrder);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "Lỗi thêm món: " + e.getMessage()));
        }
    }

    @GetMapping("/by-table/{tableId}")
    public ResponseEntity<?> getOrderByTable(@PathVariable Long tableId) {
        List<Order> orders = orderService.getOrdersByTable(tableId);

        // Lọc hóa đơn chưa thanh toán / chưa hủy
        Order active = orders.stream()
                .filter(o -> o.getStatus() != OrderStatus.PAID && o.getStatus() != OrderStatus.CANCELLED)
                .findFirst()
                .orElse(null);

        Map<String, Object> res = new HashMap<>();
        res.put("success", true);
        res.put("data", active);

        return ResponseEntity.ok(res);
    }

}