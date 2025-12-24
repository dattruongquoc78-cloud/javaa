package com.example.exaple06.Service;

import com.example.exaple06.dto.BillRequest;
import com.example.exaple06.entity.Bill;
import com.example.exaple06.entity.Order;
import com.example.exaple06.entity.Promotion;
import com.example.exaple06.repository.BillRepository;
import com.example.exaple06.repository.OrderRepository;
import com.example.exaple06.repository.PromotionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class BillService {

    private final BillRepository billRepository;
    private final OrderRepository orderRepository;
    private final PromotionRepository promotionRepository;

    public Bill createBill(BillRequest request) {
        Order order = orderRepository.findById(request.getOrderId())
                .orElseThrow(() -> new RuntimeException("❌ Đơn hàng không tồn tại"));

        double total = calculateTotal(order);
        Promotion promo = null;

        // ✅ Áp dụng KM nếu có
        if (request.getPromotionId() != null) {
            promo = promotionRepository.findById(request.getPromotionId())
                    .orElseThrow(() -> new RuntimeException("❌ Khuyến mãi không tồn tại"));

            if (!promo.getIsActive()) {
                throw new RuntimeException("❌ Khuyến mãi đã bị vô hiệu hóa");
            }

            LocalDateTime now = LocalDateTime.now();
            if (promo.getStartDate() != null && now.isBefore(promo.getStartDate())) {
                throw new RuntimeException("❌ Khuyến mãi chưa bắt đầu");
            }
            if (promo.getEndDate() != null && now.isAfter(promo.getEndDate())) {
                throw new RuntimeException("❌ Khuyến mãi đã hết hạn");
            }

            if (promo.getUsageLimit() != null && promo.getUsedCount() >= promo.getUsageLimit()) {
                throw new RuntimeException("❌ Khuyến mãi đã hết lượt sử dụng");
            }

            // ✅ Tính giảm giá
            if (promo.getDiscountType() == Promotion.DiscountType.PERCENTAGE) {
                total -= total * (promo.getDiscountPercentage() / 100);
            } else {
                total -= promo.getDiscountAmount();
            }

            total = Math.max(total, 0); // ✅ Giảm tối đa = 0
            promo.setUsedCount(promo.getUsedCount() + 1);
            promotionRepository.save(promo);
        }

        Bill bill = new Bill();
        bill.setBillNumber(generateBillNumber());
        bill.setOrder(order);
        bill.setPromotion(promo); // ✅ LƯU PROMOTION VÀO BILL
        bill.setTotalAmount(total);
        bill.setCreatedAt(LocalDateTime.now());
        bill.setPaymentMethod(request.getPaymentMethod());
        bill.setPaymentStatus("UNPAID");

        return billRepository.save(bill);
    }

    private double calculateTotal(Order order) {
        return order.getOrderDetails().stream()
                .mapToDouble(item -> item.getPrice() * item.getQuantity())
                .sum();
    }

    private String generateBillNumber() {
        return "BILL-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }

    public List<Bill> getAll() {
        return billRepository.findAll();
    }

    public Optional<Bill> getById(Long id) {
        return billRepository.findById(id);
    }

    public void delete(Long id) {
        billRepository.deleteById(id);
    }
    public Optional<Bill> getByOrderId(Long orderId) {
        return billRepository.findByOrderId(orderId);
    }
}
