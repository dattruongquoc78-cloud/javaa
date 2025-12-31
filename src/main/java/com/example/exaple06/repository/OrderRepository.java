package com.example.exaple06.repository;

import com.example.exaple06.entity.Order;
import com.example.exaple06.enums.OrderStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {

    List<Order> findByStatus(OrderStatus status);

    List<Order> findByUserId(Long userId);

    List<Order> findByTableId(Long tableId);

    List<Order> findByCreatedAtBetween(LocalDateTime start, LocalDateTime end);

    // OrderRepository.java
    List<Order> findByUserIdOrderByCreatedAtDesc(Long userId);

    // ✅ Đếm đơn theo trạng thái
    long countByStatus(OrderStatus status);

    // ✅ Tổng doanh thu từ các đơn PAID trong khoảng thời gian
    @Query("SELECT SUM(o.totalAmount) FROM Order o " +
            "WHERE o.status = :status AND o.updatedAt >= :startDate")
    Double sumByStatusAndDate(
            @Param("status") OrderStatus status,
            @Param("startDate") LocalDateTime startDate);

    List<Order> findByTableIdOrderByCreatedAtDesc(Long tableId);

}
