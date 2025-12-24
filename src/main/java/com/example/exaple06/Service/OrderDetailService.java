package com.example.exaple06.Service;

import com.example.exaple06.entity.OrderDetail;
import com.example.exaple06.entity.Product;
import com.example.exaple06.repository.OrderDetailRepository;
import com.example.exaple06.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class OrderDetailService {
    
    private final OrderDetailRepository orderDetailRepository;
    private final ProductRepository productRepository;

    public List<OrderDetail> getAll() {
        return orderDetailRepository.findAll();
    }

    public Optional<OrderDetail> getById(Long id) {
        return orderDetailRepository.findById(id);
    }

    @Transactional
    public OrderDetail save(OrderDetail orderDetail) {
        // ✅ Validate product và số lượng
        if (orderDetail.getProduct() == null || orderDetail.getProduct().getId() == null) {
            throw new IllegalArgumentException("Product không được để trống");
        }
        
        Product product = productRepository.findById(orderDetail.getProduct().getId())
                .orElseThrow(() -> new RuntimeException("Product không tồn tại với ID: " + orderDetail.getProduct().getId()));
        
        // ✅ Kiểm tra số lượng tồn kho
        if (product.getStockQuantity() < orderDetail.getQuantity()) {
            throw new RuntimeException("Sản phẩm " + product.getTitle() + " không đủ số lượng. Tồn kho: " + product.getStockQuantity());
        }
        
        // ✅ Set giá từ product (tránh client gửi giá sai)
        orderDetail.setPrice(product.getPrice());
        
        // ✅ Trừ số lượng tồn kho
        product.setStockQuantity(product.getStockQuantity() - orderDetail.getQuantity());
        productRepository.save(product);
        
        return orderDetailRepository.save(orderDetail);
    }

    public void delete(Long id) {
        OrderDetail orderDetail = orderDetailRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("OrderDetail không tồn tại"));
        
        // ✅ Restore stock quantity khi xóa order detail
        restoreProductStock(orderDetail);
        
        orderDetailRepository.deleteById(id);
    }

    // ✅ Lấy tất cả order details theo order ID
    public List<OrderDetail> getByOrderId(Long orderId) {
        return orderDetailRepository.findByOrder_Id(orderId);
    }

    // ✅ Lấy tất cả order details theo product ID
    public List<OrderDetail> getByProductId(Long productId) {
        return orderDetailRepository.findByProductId(productId);
    }

    // ✅ SỬA LỖI: Tính tổng số lượng đã bán của sản phẩm
    public Long getTotalSoldQuantityByProduct(Long productId) {
        Long total = orderDetailRepository.sumQuantityByProductId(productId);
        return total != null ? total : 0L;
    }

    // ✅ Cập nhật số lượng order detail
    @Transactional
    public OrderDetail updateQuantity(Long id, Integer newQuantity) {
        return orderDetailRepository.findById(id)
                .map(orderDetail -> {
                    int oldQuantity = orderDetail.getQuantity();
                    
                    // Restore stock cũ trước
                    Product product = orderDetail.getProduct();
                    product.setStockQuantity(product.getStockQuantity() + oldQuantity);
                    
                    // Kiểm tra stock mới
                    if (product.getStockQuantity() < newQuantity) {
                        throw new RuntimeException("Không đủ số lượng tồn kho");
                    }
                    
                    // Cập nhật số lượng mới và trừ stock
                    orderDetail.setQuantity(newQuantity);
                    product.setStockQuantity(product.getStockQuantity() - newQuantity);
                    productRepository.save(product);
                    
                    return orderDetailRepository.save(orderDetail);
                })
                .orElseThrow(() -> new RuntimeException("OrderDetail không tồn tại"));
    }

    // ✅ Helper method để restore stock
    private void restoreProductStock(OrderDetail orderDetail) {
        Product product = orderDetail.getProduct();
        product.setStockQuantity(product.getStockQuantity() + orderDetail.getQuantity());
        productRepository.save(product);
    }

    // ✅ Tính tổng tiền của order details
    public Double calculateOrderTotal(Long orderId) {
        List<OrderDetail> orderDetails = getByOrderId(orderId);
        return orderDetails.stream()
                .mapToDouble(detail -> detail.getPrice() * detail.getQuantity())
                .sum();
    }
}