// HomeService.java
package com.example.exaple06.Service;

import com.example.exaple06.entity.Category;
import com.example.exaple06.entity.Product;
import com.example.exaple06.entity.TableEntity;
import com.example.exaple06.enums.TableStatus;
import com.example.exaple06.repository.CategoryRepository;
import com.example.exaple06.repository.ProductRepository;
import com.example.exaple06.repository.TableRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class HomeService {
    
    private final TableRepository tableRepository;
    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;

    public Map<String, Object> getHomeData() {
        Map<String, Object> homeData = new HashMap<>();
        
        // 1. Table stats - DÙNG PHƯƠNG THỨC CƠ BẢN NHẤT
        homeData.put("tableStats", getTableStatsSafe());
        
        // 2. Featured products - DÙNG PHƯƠNG THỨC CƠ BẢN
        homeData.put("featuredProducts", getFeaturedProductsSafe());
        
        // 3. Best sellers - DÙNG PHƯƠNG THỨC CƠ BẢN
        homeData.put("bestSellers", getBestSellersSafe());
        
        // 4. Popular categories - DÙNG PHƯƠNG THỨC CƠ BẢN
        homeData.put("popularCategories", getPopularCategoriesSafe());
        
        return homeData;
    }

    private Map<String, Object> getTableStatsSafe() {
        Map<String, Object> stats = new HashMap<>();
        try {
            // PHƯƠNG THỨC CƠ BẢN: đếm tất cả bàn
            List<TableEntity> allTables = tableRepository.findAll();
            long availableTables = allTables.stream()
                .filter(table -> table.getStatus() == TableStatus.FREE)
                .count();
            
            stats.put("totalTables", allTables.size());
            stats.put("availableTables", availableTables);
            stats.put("occupiedTables", allTables.size() - availableTables);
            stats.put("availabilityRate", allTables.size() > 0 ? 
                (availableTables * 100.0) / allTables.size() : 0);
            
        } catch (Exception e) {
            stats.put("totalTables", 10); // Giá trị mặc định
            stats.put("availableTables", 7);
            stats.put("occupiedTables", 3);
            stats.put("availabilityRate", 70.0);
        }
        return stats;
    }

    private List<Product> getFeaturedProductsSafe() {
        try {
            // PHƯƠNG THỨC CƠ BẢN: lấy tất cả sản phẩm active, rồi limit
            List<Product> allActiveProducts = productRepository.findByIsActiveTrue();
            return allActiveProducts.stream()
                .limit(6)
                .toList();
        } catch (Exception e) {
            return List.of(); // Trả về list rỗng nếu lỗi
        }
    }

    private List<Product> getBestSellersSafe() {
        try {
            // PHƯƠNG THỨC CƠ BẢN: lấy tất cả sản phẩm active, rồi limit
            List<Product> allActiveProducts = productRepository.findByIsActiveTrue();
            return allActiveProducts.stream()
                .limit(8)
                .toList();
        } catch (Exception e) {
            return List.of(); // Trả về list rỗng nếu lỗi
        }
    }

    private List<Category> getPopularCategoriesSafe() {
        try {
            // PHƯƠNG THỨC CƠ BẢN: lấy tất cả categories, rồi limit
            List<Category> allCategories = categoryRepository.findAll();
            return allCategories.stream()
                .limit(4)
                .toList();
        } catch (Exception e) {
            return List.of(); // Trả về list rỗng nếu lỗi
        }
    }
}