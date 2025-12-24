package com.example.exaple06.Service;

import com.example.exaple06.entity.Order;
import com.example.exaple06.enums.OrderStatus;
import java.util.List;
import java.util.Optional;
import com.example.exaple06.dto.OrderRequest;

public interface OrderService {
    List<Order> getAllOrders();
    Optional<Order> getOrderById(Long id);
    Order createOrder(Order order);
    Order updateOrder(Long id, Order updatedOrder);
    void deleteOrder(Long id);

    // ✅ Cập nhật trạng thái đơn hàng
    Order updateOrderStatus(Long id, OrderStatus status);

    // ✅ Lấy đơn hàng theo trạng thái
    List<Order> getOrdersByStatus(OrderStatus status);

    // ✅ Lấy đơn hàng theo người dùng
    List<Order> getOrdersByUser(Long userId);

    // ✅ Lấy đơn hàng theo customerId (SORTED) - THÊM DÒNG NÀY
    List<Order> getOrdersByCustomerId(Long customerId);

    Order createOrderFromRequest(OrderRequest request);

    List<Order> searchOrders(String keyword);

    // ✅ THÊM DÒNG NÀY ↓↓↓
    Order save(Order order);
    // ✅ Thêm món vào order đang mở
Order addItemsToOrder(Long orderId, OrderRequest request);
List<Order> getOrdersByTable(Long tableId);


}