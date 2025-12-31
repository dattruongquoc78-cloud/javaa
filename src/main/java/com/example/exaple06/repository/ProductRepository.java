package com.example.exaple06.repository;

import com.example.exaple06.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long>, JpaSpecificationExecutor<Product> {
    // ✅ THÊM: Tìm sản phẩm theo danh mục
    List<Product> findByCategoryId(Long categoryId);
    
    // ✅ THÊM: Tìm sản phẩm đang active
    List<Product> findByIsActiveTrue();
    
    // ✅ THÊM: Tìm sản phẩm còn hàng
    List<Product> findByStockQuantityGreaterThan(Integer quantity);
    
    // ✅ THÊM: Tìm sản phẩm theo tên (tìm kiếm)
    List<Product> findByTitleContainingIgnoreCase(String title);
    
    // ✅ THÊM: Lấy giá trung bình (cho thống kê)
    @Query("SELECT COALESCE(AVG(p.price), 0) FROM Product p WHERE p.isActive = true")
    Double getAveragePrice();
    
    // ✅ THÊM: Tính tổng giá trị tồn kho
    @Query("SELECT COALESCE(SUM(p.price * p.stockQuantity), 0) FROM Product p WHERE p.isActive = true")
    Double getTotalInventoryValue();
    
    // ✅ THÊM: Lấy sản phẩm có giá cao nhất
    @Query("SELECT p FROM Product p WHERE p.isActive = true AND p.price = (SELECT MAX(p2.price) FROM Product p2 WHERE p2.isActive = true)")
    Optional<Product> findMostExpensiveProduct();
    
    // ✅ THÊM: Lấy sản phẩm có số lượng nhiều nhất
    @Query("SELECT p FROM Product p WHERE p.isActive = true AND p.stockQuantity = (SELECT MAX(p2.stockQuantity) FROM Product p2 WHERE p2.isActive = true)")
    Optional<Product> findMostStockedProduct();
    
    // ✅ THÊM: Lọc theo khoảng giá (custom query)
    @Query("SELECT p FROM Product p WHERE p.isActive = true " +
           "AND (:minPrice IS NULL OR p.price >= :minPrice) " +
           "AND (:maxPrice IS NULL OR p.price <= :maxPrice) " +
           "AND (:minQuantity IS NULL OR p.stockQuantity >= :minQuantity) " +
           "AND (:maxQuantity IS NULL OR p.stockQuantity <= :maxQuantity)")
    List<Product> filterProducts(
            @Param("minPrice") Double minPrice,
            @Param("maxPrice") Double maxPrice,
            @Param("minQuantity") Integer minQuantity,
            @Param("maxQuantity") Integer maxQuantity);
}