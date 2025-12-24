package com.example.exaple06.Service;

import com.example.exaple06.dto.OrderRequest;
import com.example.exaple06.entity.*;
import com.example.exaple06.enums.OrderStatus;
import com.example.exaple06.enums.TableStatus;
import com.example.exaple06.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;
    private final TableRepository tableRepository;
    private final UserRepository userRepository;
    private final PromotionRepository promotionRepository;
    private final SimpMessagingTemplate messagingTemplate;

    @Override
    public List<Order> getAllOrders() {
        return orderRepository.findAll();
    }

    @Override
    public Optional<Order> getOrderById(Long id) {
        return orderRepository.findById(id);
    }

    // ✅ Tạo đơn từ Request → GIỮ NGUYÊN LOGIC CŨ
    @Override
    @Transactional
    public Order createOrderFromRequest(OrderRequest req) {

        Order order = new Order();
        order.setStatus(OrderStatus.PENDING);
        order.setNotes(req.getNotes());

        // 🪑 Gắn bàn
        if (req.getTableId() != null) {
            TableEntity table = tableRepository.findById(req.getTableId())
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy bàn ID = " + req.getTableId()));
            order.setTable(table);
        }

        // 👤 Gắn user
        if (req.getUserId() != null) {
            User user = userRepository.findById(req.getUserId())
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy người dùng ID = " + req.getUserId()));
            order.setUser(user);
        }

        // ✅ Gắn khuyến mãi nếu có
        if (req.getPromotionId() != null) {
            Promotion promo = promotionRepository.findById(req.getPromotionId()).orElse(null);
            order.setPromotion(promo);
        }

        // ✅ Gắn items + giảm tồn kho
        List<OrderDetail> details = req.getItems().stream().map(item -> {
            OrderDetail d = new OrderDetail();

            Product p = productRepository.findById(item.getProductId())
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy sản phẩm!"));

            d.setProduct(p);
            d.setQuantity(item.getQuantity());

            // 🔥 nếu frontend đã gửi giá giảm thì lưu, nếu không thì dùng giá gốc
            if (item.getFinalPrice() != null && item.getFinalPrice() > 0) {
                d.setPrice(item.getFinalPrice());
            } else {
                d.setPrice(p.getPrice());
            }

            d.setOrder(order);

            p.setStockQuantity(p.getStockQuantity() - item.getQuantity());
            productRepository.save(p);

            return d;
        }).toList();

        order.setOrderDetails(details);

        // ✅ Tính tổng tiền ban đầu
        // ✅ Tính tổng tiền ban đầu
        double total = details.stream().mapToDouble(d -> d.getPrice() * d.getQuantity()).sum();
        order.setTotalAmount(total);
        order.setFinalAmount(total);

        // ✅ Nếu có khuyến mãi thì áp dụng ngay khi tạo đơn
        if (order.getPromotion() != null) {
            applyPromotion(order);
        }

        Order saved = orderRepository.save(order);

        // ✅ Chuyển bàn sang đã có khách
        if (order.getTable() != null) {
            TableEntity t = order.getTable();
            t.setStatus(TableStatus.OCCUPIED);
            tableRepository.save(t);

            messagingTemplate.convertAndSend("/topic/tables/status", t);
        }

        // 🔔 WebSocket gửi đơn mới
        messagingTemplate.convertAndSend("/topic/orders", saved);

        return saved;
    }

    // ✅ Cập nhật trạng thái đơn
    @Override
    @Transactional
    public Order updateOrderStatus(Long id, OrderStatus status) {
        return orderRepository.findById(id)
                .map(order -> {

                    order.setStatus(status);

                    // ✅ Nếu là thanh toán → áp dụng giảm giá
                    if (status == OrderStatus.PAID) {
                        applyPromotion(order);
                    }

                    // ✅ Nếu thanh toán hoặc huỷ → bàn trở lại rảnh
                    if ((status == OrderStatus.PAID || status == OrderStatus.CANCELLED)
                            && order.getTable() != null) {

                        TableEntity t = order.getTable();
                        t.setStatus(TableStatus.FREE);
                        tableRepository.save(t);

                        messagingTemplate.convertAndSend("/topic/tables/status", t);
                    }

                    messagingTemplate.convertAndSend("/topic/orders/status", order);
                    return orderRepository.save(order);
                })
                .orElseThrow(() -> new RuntimeException("Không tìm thấy đơn hàng!"));
    }

    // ✅ Áp dụng khuyến mãi khi thanh toán
    private void applyPromotion(Order order) {
        Promotion promo = order.getPromotion();
        if (promo == null)
            return;

        double total = order.getTotalAmount();
        double finalPrice = total;

        if (promo.getDiscountType() == Promotion.DiscountType.PERCENTAGE) {
            finalPrice = total - (total * promo.getDiscountPercentage() / 100);
        } else {
            finalPrice = total - promo.getDiscountAmount();
        }

        if (finalPrice < 0)
            finalPrice = 0d;
        order.setFinalAmount(finalPrice);

        // ✅ Tăng lượt sử dụng
        promo.setUsedCount(promo.getUsedCount() + 1);
        promotionRepository.save(promo);
    }

    @Override
    public Order updateOrder(Long id, Order updatedOrder) {
        return orderRepository.findById(id)
                .map(order -> {
                    order.setStatus(updatedOrder.getStatus());
                    order.setNotes(updatedOrder.getNotes());
                    return orderRepository.save(order);
                })
                .orElseThrow(() -> new RuntimeException("Không tìm thấy đơn hàng ID = " + id));
    }

    @Override
    public void deleteOrder(Long id) {
        orderRepository.deleteById(id);
    }

    @Override
    public List<Order> getOrdersByStatus(OrderStatus status) {
        return orderRepository.findByStatus(status);
    }

    @Override
    public List<Order> getOrdersByUser(Long userId) {
        return orderRepository.findByUserId(userId);
    }

    @Override
    public List<Order> searchOrders(String keyword) {
        // ✅ Tạo biến final để dùng trong lambda
        final String searchKey = keyword.toLowerCase();

        return orderRepository.findAll().stream()
                .filter(o -> String.valueOf(o.getId()).contains(searchKey)
                        || (o.getTable() != null && o.getTable().getName().toLowerCase().contains(searchKey))
                        || (o.getStatus() != null && o.getStatus().name().toLowerCase().contains(searchKey)))
                .toList();
    }

    @Override
    public List<Order> getOrdersByCustomerId(Long customerId) {
        return orderRepository.findByUserIdOrderByCreatedAtDesc(customerId);
    }

    @Override
    public Order save(Order order) {
        return orderRepository.save(order);
    }

    @Override
    @Transactional
    public Order createOrder(Order order) {
        // ✅ Tổng tiền từ chi tiết đơn hàng
        double total = order.getOrderDetails().stream()
                .mapToDouble(d -> d.getPrice() * d.getQuantity())
                .sum();
        order.setTotalAmount(total);

        // ✅ Nếu chưa đặt trạng thái → mặc định PENDING
        if (order.getStatus() == null) {
            order.setStatus(OrderStatus.PENDING);
        }

        // ✅ Lưu đơn hàng ban đầu
        Order saved = orderRepository.save(order);

        return saved;
    }

    @Override
    @Transactional
    public Order addItemsToOrder(Long orderId, OrderRequest req) {

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy hóa đơn!"));

        // ❌ Không cho thêm món nếu hóa đơn đã thanh toán hoặc hủy
        if (order.getStatus() == OrderStatus.PAID || order.getStatus() == OrderStatus.CANCELLED) {
            throw new RuntimeException("Không thể thêm món vì hóa đơn đã đóng!");
        }

        // 🔄 Thêm từng item vào order tồn tại
        req.getItems().forEach(item -> {
            Product product = productRepository.findById(item.getProductId())
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy sản phẩm!"));

            OrderDetail detail = new OrderDetail();
            detail.setOrder(order);
            detail.setProduct(product);
            detail.setQuantity(item.getQuantity());
detail.setPrice(
    item.getFinalPrice() != null && item.getFinalPrice() > 0
            ? item.getFinalPrice()
            : product.getPrice()
);

            order.getOrderDetails().add(detail);

            // 📉 Trừ số lượng tồn kho
            product.setStockQuantity(product.getStockQuantity() - item.getQuantity());
            productRepository.save(product);
        });

        // 🧮 Cập nhật lại tổng tiền
        double total = order.getOrderDetails().stream()
                .mapToDouble(d -> d.getPrice() * d.getQuantity())
                .sum();

        order.setTotalAmount(total);
        order.setFinalAmount(total);

        Order saved = orderRepository.save(order);

        // 🔔 Gửi cập nhật realtime qua WebSocket
        messagingTemplate.convertAndSend("/topic/orders", saved);

        return saved;
    }

    @Override
    public List<Order> getOrdersByTable(Long tableId) {
        return orderRepository.findByTableIdOrderByCreatedAtDesc(tableId);
    }

}
