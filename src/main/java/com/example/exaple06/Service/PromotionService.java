package com.example.exaple06.Service;

import com.example.exaple06.entity.Promotion;
import com.example.exaple06.repository.PromotionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class PromotionService {

    private final PromotionRepository promotionRepository;
    private final SimpMessagingTemplate messaging;

    // ✅ Lấy tất cả khuyến mãi
    public List<Promotion> getAllPromotions() {
        return promotionRepository.findAll();
    }

    // ✅ Lấy khuyến mãi theo ID
    public Optional<Promotion> getPromotionById(Long id) {
        return promotionRepository.findById(id);
    }

    // ✅ Tạo khuyến mãi mới
    public Promotion createPromotion(Promotion promotion) {
        promotion.setIsActive(true);
        promotion.setUsedCount(0);
        return promotionRepository.save(promotion);
    }

    // ✅ Cập nhật khuyến mãi
    public Promotion updatePromotion(Long id, Promotion promotionDetails) {
        return promotionRepository.findById(id)
            .map(promotion -> {
                promotion.setName(promotionDetails.getName());
                promotion.setDescription(promotionDetails.getDescription());
                promotion.setCode(promotionDetails.getCode());
                promotion.setDiscountType(promotionDetails.getDiscountType());
                promotion.setDiscountAmount(promotionDetails.getDiscountAmount());
                promotion.setDiscountPercentage(promotionDetails.getDiscountPercentage());
                promotion.setStartDate(promotionDetails.getStartDate());
                promotion.setEndDate(promotionDetails.getEndDate());
                promotion.setIsActive(promotionDetails.getIsActive());
                promotion.setUsageLimit(promotionDetails.getUsageLimit());
                return promotionRepository.save(promotion);
            })
            .orElseThrow(() -> new RuntimeException("Promotion không tồn tại"));
    }

    // ✅ Xóa mềm khuyến mãi
    public void deletePromotion(Long id) {
        Promotion promotion = promotionRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Promotion không tồn tại"));
        promotion.setIsActive(false);
        promotionRepository.save(promotion);
    }

    // ✅ Lấy danh sách khuyến mãi đang diễn ra
    public List<Promotion> getActivePromotions() {
        LocalDateTime now = LocalDateTime.now();
        return promotionRepository.findByIsActiveTrueAndStartDateBeforeAndEndDateAfter(now, now);
    }

    // ✅ Tìm kiếm khuyến mãi theo tên
    public List<Promotion> searchPromotions(String name) {
        return promotionRepository.findByNameContainingIgnoreCase(name);
    }

    // ✅ Kiểm tra và sử dụng mã khuyến mãi
    public Optional<Promotion> validateAndUsePromotion(String code) {
        LocalDateTime now = LocalDateTime.now();

        return promotionRepository.findByCode(code)
            .filter(Promotion::getIsActive)
            .filter(p -> !now.isBefore(p.getStartDate()) && !now.isAfter(p.getEndDate()))
            .filter(p -> p.getUsageLimit() == null || p.getUsedCount() < p.getUsageLimit())
            .map(p -> {
                p.setUsedCount(p.getUsedCount() + 1);
                if (p.getUsageLimit() != null && p.getUsedCount() >= p.getUsageLimit()) {
                    p.setIsActive(false);
                    promotionRepository.save(p);
                    messaging.convertAndSend("/topic/promotions",
                        Map.of("type", "LIMIT_REACHED", "id", p.getId(), "code", p.getCode(), "name", p.getName()));
                    return p;
                }
                return promotionRepository.save(p);
            });
    }

    // ✅ Tìm khuyến mãi theo mã
    public Optional<Promotion> findByCode(String code) {
        return promotionRepository.findByCode(code);
    }

    // ✅ Vô hiệu hóa các khuyến mãi đã hết hạn
    public void deactivateExpiredNow() {
        LocalDateTime now = LocalDateTime.now();
        List<Promotion> expired = promotionRepository.findByIsActiveTrueAndEndDateBefore(now);
        if (!expired.isEmpty()) {
            expired.forEach(p -> p.setIsActive(false));
            promotionRepository.saveAll(expired);
            messaging.convertAndSend("/topic/promotions",
                Map.of("type", "EXPIRED_BATCH", "ids",
                    expired.stream().map(Promotion::getId).toList()));
        }
    }

    // ✅ Lấy danh sách khuyến mãi sắp hết hạn trong X ngày
    public List<Promotion> getAlmostExpired(int days) {
        LocalDateTime now = LocalDateTime.now();
        return promotionRepository.findAlmostExpired(now, now.plusDays(days));
    }
    // ✅ Tự động vô hiệu hóa khuyến mãi hết hạn
@Scheduled(cron = "0 * * * * *") // chạy mỗi phút
public void autoDeactivateExpired() {
    LocalDateTime now = LocalDateTime.now();
    List<Promotion> expiredList = promotionRepository.findAll().stream()
            .filter(p -> p.getEndDate() != null && p.getEndDate().isBefore(now))
            .filter(Promotion::getIsActive)
            .toList();

    if (!expiredList.isEmpty()) {
        expiredList.forEach(p -> p.setIsActive(false));
        promotionRepository.saveAll(expiredList);
        System.out.println("⛔ Auto-disable: " + expiredList.size() + " mã KM đã hết hạn");
    }
}

}
