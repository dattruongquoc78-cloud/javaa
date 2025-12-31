package com.example.exaple06.controller;

import com.example.exaple06.Service.ProductService;
import com.example.exaple06.dto.ApiResponse;
import com.example.exaple06.entity.Product;

import lombok.RequiredArgsConstructor;

import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.server.mvc.WebMvcLinkBuilder;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.Map;
import java.util.List;


@CrossOrigin(origins = {"http://localhost:3000", "http://localhost:3001"})
@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
public class ProductController {
    
    private final ProductService productService;

    @GetMapping
    public ResponseEntity<ApiResponse> getAllProducts() {
        try {
            List<Product> products = productService.getAllProducts();
            return ResponseEntity.ok(ApiResponse.success("✅ Lấy danh sách sản phẩm thành công", products));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error("❌ Lỗi: " + e.getMessage()));
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse> getProductById(@PathVariable Long id) {
        try {
            return productService.getProductById(id)
                    .map(product -> ResponseEntity.ok(ApiResponse.success("✅ Lấy sản phẩm thành công", product)))
                    .orElse(ResponseEntity.notFound().build());
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error("❌ Lỗi: " + e.getMessage()));
        }
    }

    @PostMapping
    public ResponseEntity<ApiResponse> createProduct(@RequestBody Product product) {
        try {
            Product savedProduct = productService.createProduct(product);
            return ResponseEntity.status(201).body(ApiResponse.success("✅ Tạo sản phẩm thành công", savedProduct));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error("❌ Lỗi tạo sản phẩm: " + e.getMessage()));
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse> updateProduct(@PathVariable Long id, @RequestBody Product product) {
        try {
            Product updated = productService.updateProduct(id, product);
            return ResponseEntity.ok(ApiResponse.success("✅ Cập nhật sản phẩm thành công", updated));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error("❌ Lỗi cập nhật: " + e.getMessage()));
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse> deleteProduct(@PathVariable Long id) {
        try {
            productService.deleteProduct(id);
            return ResponseEntity.ok(ApiResponse.success("✅ Xóa sản phẩm thành công"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error("❌ Lỗi xóa: " + e.getMessage()));
        }
    }

    @GetMapping("/search")
    public ResponseEntity<ApiResponse> searchProducts(@RequestParam String keyword) {
        try {
            List<Product> products = productService.searchProducts(keyword);
            return ResponseEntity.ok(ApiResponse.success("✅ Tìm kiếm thành công", products));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error("❌ Lỗi tìm kiếm: " + e.getMessage()));
        }
    }

    @GetMapping("/category/{categoryId}")
    public ResponseEntity<ApiResponse> getProductsByCategory(@PathVariable Long categoryId) {
        try {
            List<Product> products = productService.getProductsByCategory(categoryId);
            return ResponseEntity.ok(ApiResponse.success("✅ Lấy sản phẩm theo danh mục thành công", products));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error("❌ Lỗi: " + e.getMessage()));
        }
    }
     // ✅ 1. Endpoint mới có HATEOAS (v2)
    @GetMapping("/v2/{id}")
    public ResponseEntity<ApiResponse> getProductByIdV2(@PathVariable Long id) {
        try {
            return productService.getProductById(id)
                    .map(product -> {
                        EntityModel<Product> resource = EntityModel.of(product);
                        
                        // Self link
                        resource.add(WebMvcLinkBuilder.linkTo(
                            WebMvcLinkBuilder.methodOn(ProductController.class)
                                .getProductByIdV2(id)).withSelfRel());
                        
                        // Update link
                        resource.add(WebMvcLinkBuilder.linkTo(
                            WebMvcLinkBuilder.methodOn(ProductController.class)
                                .updateProduct(id, product)).withRel("update"));
                        
                        // Delete link
                        resource.add(WebMvcLinkBuilder.linkTo(
                            WebMvcLinkBuilder.methodOn(ProductController.class)
                                .deleteProduct(id)).withRel("delete"));
                        
                        // All products link
                        resource.add(WebMvcLinkBuilder.linkTo(
                            WebMvcLinkBuilder.methodOn(ProductController.class)
                                .getAllProducts()).withRel("all-products"));
                        
                        return ResponseEntity.ok(
                            ApiResponse.success("✅ Lấy sản phẩm thành công (HATEOAS)", resource)
                        );
                    })
                    .orElse(ResponseEntity.notFound().build());
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("❌ Lỗi: " + e.getMessage()));
        }
    }
    
    // ✅ 2. Endpoint lọc nâng cao MỚI
    @GetMapping("/advanced-filter")
    public ResponseEntity<ApiResponse> advancedFilterProducts(
            @RequestParam(required = false) Double minPrice,
            @RequestParam(required = false) Double maxPrice,
            @RequestParam(required = false) Integer minQuantity,
            @RequestParam(required = false) Integer maxQuantity) {
        try {
            List<Product> products = productService.filterProducts(
                minPrice, maxPrice, minQuantity, maxQuantity);
            return ResponseEntity.ok(
                ApiResponse.success("✅ Lọc nâng cao thành công", products)
            );
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("❌ Lỗi lọc: " + e.getMessage()));
        }
    }
    
    // ✅ 3. Endpoint thống kê MỚI
    @GetMapping("/statistics")
    public ResponseEntity<ApiResponse> getStatistics() {
        try {
            Map<String, Object> stats = productService.getProductStats();
            return ResponseEntity.ok(
                ApiResponse.success("✅ Thống kê thành công", stats)
            );
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("❌ Lỗi thống kê: " + e.getMessage()));
        }
    }
}