package com.example.exaple06.Service;

import com.example.exaple06.entity.Category;
import com.example.exaple06.entity.Product;
import com.example.exaple06.repository.CategoryRepository;
import com.example.exaple06.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

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
}