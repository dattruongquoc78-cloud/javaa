package com.example.exaple06.repository;

import com.example.exaple06.entity.OrderDetail;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OrderDetailRepository extends JpaRepository<OrderDetail, Long> {
    
    // ✅ ĐÚNG: Tìm order details theo order ID
    List<OrderDetail> findByOrder_Id(Long orderId);
    
    // ✅ ĐÚNG: Tìm order details theo product ID
    List<OrderDetail> findByProductId(Long productId);
    
    // ✅ SỬA LỖI: Tính tổng số lượng đã bán của sản phẩm
    @Query("SELECT SUM(od.quantity) FROM OrderDetail od WHERE od.product.id = :productId")
    Long sumQuantityByProductId(@Param("productId") Long productId);
    
    // ✅ THÊM: Tìm order details theo order status (nếu cần)
    @Query("SELECT od FROM OrderDetail od WHERE od.order.status = :status")
    List<OrderDetail> findByOrderStatus(@Param("status") String status);
}