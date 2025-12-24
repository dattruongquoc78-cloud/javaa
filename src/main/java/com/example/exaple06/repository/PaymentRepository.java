package com.example.exaple06.repository;

import com.example.exaple06.entity.Payment;
import com.example.exaple06.enums.PaymentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long> {
    // ✅ THÊM: Tìm payment theo trạng thái
    List<Payment> findByStatus(PaymentStatus status);
    
    // ✅ THÊM: Tìm payment theo phương thức
    List<Payment> findByMethod(String method);
    
    // ✅ THÊM: Tìm payment trong khoảng thời gian
    List<Payment> findByIssuedAtBetween(LocalDateTime start, LocalDateTime end);
    Payment findByTransactionId(String transactionId);
    Payment findByOrderId(Long orderId);

}