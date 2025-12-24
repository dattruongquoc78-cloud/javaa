package com.example.exaple06.controller;

import com.example.exaple06.enums.OrderStatus;
import com.example.exaple06.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.Map;

@RestController
@RequestMapping("/api/report")
@CrossOrigin(origins = "http://localhost:3000")
@RequiredArgsConstructor
public class ReportController {

    private final OrderRepository orderRepository;

    @GetMapping("/summary")
    public ResponseEntity<?> getSummary(@RequestParam(defaultValue = "7") int days) {

        LocalDate startDate = LocalDate.now().minusDays(days);

        long totalOrders = orderRepository.countByStatus(OrderStatus.PAID);
        long totalPending = orderRepository.countByStatus(OrderStatus.PENDING);
        long totalCancelled = orderRepository.countByStatus(OrderStatus.CANCELLED);
        Double totalRevenue = orderRepository.sumByStatusAndDate(
                OrderStatus.PAID, startDate.atStartOfDay()
        );

        return ResponseEntity.ok(Map.of(
                "success", true,
                "totalOrders", totalOrders,
                "totalPending", totalPending,
                "totalCancelled", totalCancelled,
                "totalRevenue", totalRevenue == null ? 0 : totalRevenue
        ));
    }
}
