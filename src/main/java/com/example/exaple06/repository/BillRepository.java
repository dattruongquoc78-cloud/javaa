package com.example.exaple06.repository;

import com.example.exaple06.entity.Bill;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface BillRepository extends JpaRepository<Bill, Long> {

    Optional<Bill> findByBillNumber(String billNumber);

    Optional<Bill> findByOrderId(Long orderId);

    List<Bill> findByCreatedAtBetween(LocalDateTime start, LocalDateTime end);

}
