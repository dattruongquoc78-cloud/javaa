package com.example.exaple06.Service;

import com.example.exaple06.entity.Category;
import com.example.exaple06.entity.Product;
import com.example.exaple06.repository.CategoryRepository;
import com.example.exaple06.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;

    public List<Product> getAllProducts() {
        return productRepository.findByIsActiveTrue();
    }

    public Optional<Product> getProductById(Long id) {
        return productRepository.findById(id);
    }

    public Product createProduct(Product product) {
        if (product.getCategory() == null || product.getCategory().getId() == null) {
            throw new IllegalArgumentException("Category ID không được để trống");
        }

        Category category = categoryRepository.findById(product.getCategory().getId())
                .orElseThrow(
                        () -> new RuntimeException("Không tìm thấy category với ID: " + product.getCategory().getId()));

        product.setCategory(category);
        product.setIsActive(true); // ✅ Mặc định active
        return productRepository.save(product);
    }

    public Product updateProduct(Long id, Product productDetails) {
        return productRepository.findById(id)
                .map(product -> {
                    product.setTitle(productDetails.getTitle());
                    product.setDescription(productDetails.getDescription());
                    product.setPrice(productDetails.getPrice());
                    product.setPhoto(productDetails.getPhoto());
                    product.setStockQuantity(productDetails.getStockQuantity());

                    if (productDetails.getCategory() != null) {
                        Category category = categoryRepository.findById(productDetails.getCategory().getId())
                                .orElseThrow(() -> new RuntimeException("Category không tồn tại"));
                        product.setCategory(category);
                    }

                    return productRepository.save(product);
                })
                .orElseThrow(() -> new RuntimeException("Product không tồn tại với ID: " + id));
    }

    public void deleteProduct(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Product không tồn tại"));
        product.setIsActive(false); // ✅ Soft delete
        productRepository.save(product);
    }

    // ✅ THÊM: Tìm sản phẩm theo category
    public List<Product> getProductsByCategory(Long categoryId) {
        return productRepository.findByCategoryId(categoryId);
    }

    // ✅ THÊM: Tìm sản phẩm theo tên
    public List<Product> searchProducts(String keyword) {
        return productRepository.findByTitleContainingIgnoreCase(keyword);
    }

    // ✅ THÊM: Cập nhật số lượng tồn kho
    public void updateStock(Long productId, Integer quantity) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Product không tồn tại"));
        product.setStockQuantity(product.getStockQuantity() + quantity);
        productRepository.save(product);
    }

    public Map<String, Object> getProductStats() {
        Map<String, Object> stats = new HashMap<>();

        // Thống kê đơn giản
        stats.put("totalProducts", productRepository.count());
        stats.put("averagePrice", productRepository.getAveragePrice());
        stats.put("totalValue", productRepository.getTotalInventoryValue());

        // Sản phẩm đắt nhất
        productRepository.findMostExpensiveProduct()
                .ifPresent(product -> {
                    stats.put("mostExpensiveProduct", product.getTitle());
                    stats.put("mostExpensivePrice", product.getPrice());
                });

        // Sản phẩm nhiều nhất
        productRepository.findMostStockedProduct()
                .ifPresent(product -> {
                    stats.put("mostStockedProduct", product.getTitle());
                    stats.put("mostStockedQuantity", product.getStockQuantity());
                });

        // Thống kê theo trạng thái
        long activeCount = productRepository.findByIsActiveTrue().size();
        stats.put("activeProducts", activeCount);
        stats.put("inactiveProducts", productRepository.count() - activeCount);

        return stats;
    }
     public List<Product> filterProducts(Double minPrice, Double maxPrice,
                                        Integer minQuantity, Integer maxQuantity) {
        // Lấy tất cả sản phẩm active
        List<Product> allActiveProducts = productRepository.findByIsActiveTrue();
        
        // Lọc trong memory (đơn giản, không ảnh hưởng code cũ)
        return allActiveProducts.stream()
            .filter(p -> minPrice == null || p.getPrice() >= minPrice)
            .filter(p -> maxPrice == null || p.getPrice() <= maxPrice)
            .filter(p -> minQuantity == null || p.getStockQuantity() >= minQuantity)
            .filter(p -> maxQuantity == null || p.getStockQuantity() <= maxQuantity)
            .collect(Collectors.toList());
    }
}