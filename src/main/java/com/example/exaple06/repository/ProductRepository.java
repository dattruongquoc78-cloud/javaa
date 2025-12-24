package com.example.exaple06.repository;

import com.example.exaple06.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {
    // ✅ THÊM: Tìm sản phẩm theo danh mục
    List<Product> findByCategoryId(Long categoryId);
    
    // ✅ THÊM: Tìm sản phẩm đang active
    List<Product> findByIsActiveTrue();
    
    // ✅ THÊM: Tìm sản phẩm còn hàng
    List<Product> findByStockQuantityGreaterThan(Integer quantity);
    
    // ✅ THÊM: Tìm sản phẩm theo tên (tìm kiếm)
    List<Product> findByTitleContainingIgnoreCase(String title);
}