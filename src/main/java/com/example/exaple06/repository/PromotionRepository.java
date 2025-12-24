package com.example.exaple06.repository;

import com.example.exaple06.entity.Promotion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface PromotionRepository extends JpaRepository<Promotion, Long> {

    // ✅ Tìm khuyến mãi đang active
    List<Promotion> findByIsActiveTrue();

    // ✅ Tìm khuyến mãi đang trong thời gian áp dụng
    List<Promotion> findByIsActiveTrueAndStartDateBeforeAndEndDateAfter(LocalDateTime now1, LocalDateTime now2);

    // ✅ Tìm khuyến mãi theo tên
    List<Promotion> findByNameContainingIgnoreCase(String name);

    // ✅ Tìm theo mã code
    Optional<Promotion> findByCode(String code);

    // ✅ Tìm khuyến mãi sắp hết hạn trong vòng X giờ/ngày
    @Query("SELECT p FROM Promotion p WHERE p.isActive = true AND p.endDate <= :until AND p.endDate >= :now")
    List<Promotion> findAlmostExpired(@Param("now") LocalDateTime now, @Param("until") LocalDateTime until);

    // ✅ Tìm khuyến mãi đã hết hạn nhưng vẫn còn active
    List<Promotion> findByIsActiveTrueAndEndDateBefore(LocalDateTime now);

    // ✅ Tìm theo loại giảm giá (PERCENTAGE hoặc FIXED_AMOUNT)
    List<Promotion> findByDiscountType(Promotion.DiscountType discountType);

    // ✅ Tìm phần trăm giảm > X
    List<Promotion> findByDiscountPercentageGreaterThan(Double percent);
}
