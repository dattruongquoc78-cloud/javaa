package com.example.exaple06.controller;

import com.example.exaple06.Service.OrderService;
import com.example.exaple06.config.PayOSConfig;
import com.example.exaple06.dto.BillRequest;
import com.example.exaple06.entity.Order;
import com.example.exaple06.entity.Payment;
import com.example.exaple06.enums.OrderStatus;
import com.example.exaple06.enums.PaymentMethod;
import com.example.exaple06.enums.PaymentStatus;
import com.example.exaple06.Service.BillService;
import com.example.exaple06.repository.PaymentRepository;
import com.stripe.Stripe;
import com.stripe.model.checkout.Session;
import com.stripe.param.checkout.SessionCreateParams;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import com.stripe.net.Webhook;
import org.springframework.web.client.RestTemplate;
import com.stripe.model.Event;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

@RestController
@RequestMapping("/api/payments")
@CrossOrigin(origins = { "http://localhost:3000" })
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentRepository paymentRepository;
    private final OrderService orderService;
    private final BillService billService;
    private final BillRequest billReq = new BillRequest();

    // ==== THÊM CÁC BIẾN CẤU HÌNH ====
    @Value("${stripe.secret.key}")
    private String stripeSecretKey;

    @Value("${stripe.publishable.key}")
    private String stripePublishableKey;

    // 🔐 Khởi tạo Stripe
    @PostConstruct
    public void init() {
        Stripe.apiKey = stripeSecretKey;
    }

    // ==== KẾT THÚC PHẦN THÊM ====
    @Autowired
    private PayOSConfig payOSConfig;
    private final SimpMessagingTemplate messagingTemplate;

    private Double getPaymentAmount(Order order) {
        return order.getFinalAmount() != null
                ? order.getFinalAmount()
                : order.getTotalAmount();
    }

    private String generateQR(Long orderId, Double amount) {
        String bankCode = "970422";
        String accountNo = "0342879925";
        String accountName = "PHAN THANH DANH";
        String content = "PAYORDER" + orderId;

        return "https://img.vietqr.io/image/" + bankCode + "-" + accountNo +
                "-qr_only.png?amount=" + amount.intValue() +
                "&addInfo=" + content +
                "&accountName=" + accountName;
    }

    // 🔐 Hàm tạo chữ ký HMAC SHA256
    private String hmacSHA256(String data, String key) {
        try {
            javax.crypto.Mac hmac = javax.crypto.Mac.getInstance("HmacSHA256");
            javax.crypto.spec.SecretKeySpec secretKey = new javax.crypto.spec.SecretKeySpec(key.getBytes(),
                    "HmacSHA256");
            hmac.init(secretKey);
            byte[] hash = hmac.doFinal(data.getBytes());
            StringBuilder sb = new StringBuilder();
            for (byte b : hash)
                sb.append(String.format("%02x", b));
            return sb.toString();
        } catch (Exception e) {
            throw new RuntimeException("Lỗi tạo chữ ký HMAC SHA256", e);
        }
    }

    private String normalize(String input) {
        if (input == null)
            return "";
        return java.text.Normalizer.normalize(input, java.text.Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "") // bỏ dấu
                .replaceAll("[^a-zA-Z0-9 ]", "") // bỏ ký tự đặc biệt như #, /, _
                .trim();
    }

    @PostMapping("/cash/{orderId}")
    public ResponseEntity<?> payCash(@PathVariable Long orderId) {
        try {
            Order order = orderService.getOrderById(orderId)
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy đơn hàng!"));

            Double amount = getPaymentAmount(order);

            Payment payment = Payment.builder()
                    .method(PaymentMethod.CASH)
                    .status(PaymentStatus.COMPLETED)
                    .totalAmount(amount)
                    .order(order)
                    .notes("Thanh toán tiền mặt tại quầy")
                    .build();

            paymentRepository.save(payment);
            order.setPayment(payment);
            orderService.updateOrderStatus(orderId, OrderStatus.PAID);
            BillRequest billReq = new BillRequest();
            billReq.setOrderId(orderId);
            // payCash()
            billReq.setPaymentMethod("CASH");
            // hoặc CASH / QR banking
            billReq.setPromotionId(order.getPromotion() != null ? order.getPromotion().getId() : null);

            billService.createBill(billReq);

            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "💵 Thanh toán tiền mặt thành công!"));

        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "❌ Lỗi thanh toán: " + e.getMessage()));
        }
    }

    @PostMapping("/qr/{orderId}")
    public ResponseEntity<?> payWithQR(@PathVariable Long orderId) {
        try {
            Order order = orderService.getOrderById(orderId)
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy đơn hàng!"));

            // 👉 Nếu đơn đã thanh toán -> không tạo QR nữa
            if (order.getPayment() != null && order.getPayment().getStatus() == PaymentStatus.COMPLETED) {
                return ResponseEntity.ok(Map.of(
                        "success", false,
                        "message", "⚠️ Đơn hàng đã thanh toán, không cần tạo QR nữa!"));
            }

            // 👉 Nếu đã có payment đang chờ (PENDING) → dùng lại, không tạo thêm
            Payment existingPayment = order.getPayment();
            // Nếu có Payment cũ nhưng chưa thanh toán -> hủy nó và tạo transaction mới
            if (existingPayment != null &&
                    existingPayment.getMethod() == PaymentMethod.QR &&
                    existingPayment.getStatus() == PaymentStatus.PENDING) {

                existingPayment.setStatus(PaymentStatus.CANCELLED);
                paymentRepository.save(existingPayment);

                System.out.println("⚠️ Payment cũ chưa thanh toán -> chuyển sang CANCELLED");
            }

            // 👉 Tính tiền đúng (có KM hay chưa)
            Double amount = getPaymentAmount(order);

            // 👉 Tạo giao dịch QR mới
            Payment payment = Payment.builder()
                    .method(PaymentMethod.QR)
                    .status(PaymentStatus.PENDING)
                    .totalAmount(amount)
                    .order(order)
                    .notes("QR Banking VietQR")
                    .build();

            paymentRepository.save(payment);
            order.setPayment(payment);
            orderService.save(order);

            // 👉 Tạo đường dẫn QR
            String qrUrl = generateQR(orderId, amount);

            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "Tạo mã QR thành công",
                    "data", Map.of(
                            "qrUrl", qrUrl,
                            "content", "PAYORDER" + orderId,
                            "orderId", orderId,
                            "amount", amount)));

        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "❌ Lỗi tạo mã QR: " + e.getMessage()));
        }
    }

    @GetMapping(value = "/qr/verify/{orderId}", produces = "application/json")
    public ResponseEntity<?> verifyPayment(@PathVariable Long orderId) {

        try {
            Order order = orderService.getOrderById(orderId)
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy đơn hàng!"));

            Payment payment = order.getPayment();
            if (payment == null) {
                return ResponseEntity.badRequest().body(Map.of(
                        "success", false,
                        "message", "❌ Chưa có thông tin giao dịch!"));
            }

            // 🚀 Đổi trạng thái thành PAID
            payment.setStatus(PaymentStatus.COMPLETED);
            paymentRepository.save(payment);

            orderService.updateOrderStatus(orderId, OrderStatus.PAID);

            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "✔ Thanh toán xác nhận thành công!"));

        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "❌ " + e.getMessage()));
        }
    }

    @PutMapping("/confirm/{orderId}")
    public ResponseEntity<?> confirmPayment(@PathVariable Long orderId) {
        try {
            orderService.updateOrderStatus(orderId, OrderStatus.PAID);

            BillRequest billReq = new BillRequest();
            billReq.setOrderId(orderId);
            billReq.setPaymentMethod("QR Banking");
            billService.createBill(billReq);

            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "Thanh toán đã xác nhận"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", e.getMessage()));
        }
    }

    @PostMapping("/stripe/{orderId}")
    public ResponseEntity<?> payWithStripe(@PathVariable Long orderId) {
        try {
            Order order = orderService.getOrderById(orderId)
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy đơn hàng!"));

            // 🚫 Nếu đơn đã thanh toán -> Không cho thanh toán nữa
            if (order.getStatus() == OrderStatus.PAID) {
                return ResponseEntity.badRequest().body(Map.of(
                        "success", false,
                        "message", "🚫 Đơn hàng đã thanh toán rồi!"));
            }

            Double amount = getPaymentAmount(order);
            long stripeAmount = amount.longValue();

            // 🔎 Kiểm tra nếu đã có payment STRIPE đang chờ thì dùng lại — KHÔNG tạo mới
            Payment existingPayment = order.getPayment();
            if (existingPayment != null &&
                    existingPayment.getMethod() == PaymentMethod.STRIPE &&
                    existingPayment.getStatus() == PaymentStatus.PENDING) {

                return ResponseEntity.ok(Map.of(
                        "success", true,
                        "url", "https://checkout.stripe.com/pay/" + existingPayment.getTransactionId(),
                        "message", "⚠️ Stripe đã tạo trước đó — tiếp tục thanh toán."));
            }

            // 👉 Nếu chưa có payment hoặc payment khác Stripe → tạo session và payment mới
            SessionCreateParams params = SessionCreateParams.builder()
                    .setMode(SessionCreateParams.Mode.PAYMENT)
                    .setSuccessUrl("http://localhost:3000/employee?stripe_success=true&orderId=" + orderId)
                    .setCancelUrl("http://localhost:3000/employee?stripe_canceled=true")
                    .addLineItem(
                            SessionCreateParams.LineItem.builder()
                                    .setQuantity(1L)
                                    .setPriceData(
                                            SessionCreateParams.LineItem.PriceData.builder()
                                                    .setCurrency("vnd")
                                                    .setUnitAmount(stripeAmount)
                                                    .setProductData(
                                                            SessionCreateParams.LineItem.PriceData.ProductData.builder()
                                                                    .setName("Đơn hàng #" + orderId)
                                                                    .build())
                                                    .build())
                                    .build())
                    .putMetadata("orderId", orderId.toString())
                    .build();

            Session session = Session.create(params);

            // 🆕 Tạo lệnh thanh toán mới
            Payment payment = Payment.builder()
                    .method(PaymentMethod.STRIPE)
                    .status(PaymentStatus.PENDING)
                    .totalAmount(amount)
                    .order(order)
                    .transactionId(session.getId()) // LƯU ID SESSION
                    .notes("Stripe Checkout")
                    .build();

            paymentRepository.save(payment);

            // Gán ngược vào order
            order.setPayment(payment);
            orderService.save(order);

            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "url", session.getUrl()));

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "❌ Lỗi thanh toán Stripe: " + e.getMessage()));
        }
    }

    @PostMapping("/stripe/webhook")
    public ResponseEntity<String> handleStripeWebhook(@RequestBody String payload,
            @RequestHeader("Stripe-Signature") String sigHeader) {
        try {
            String endpointSecret = "whsec_xxx"; // Sẽ lấy từ Stripe Dashboard

            Event event = Webhook.constructEvent(payload, sigHeader, endpointSecret);

            if ("checkout.session.completed".equals(event.getType())) {
                Session session = (Session) event.getDataObjectDeserializer().getObject().get();

                // SỬA DÒNG NÀY: bỏ chữ "key:"
                String orderIdStr = session.getMetadata().get("orderId");
                Long orderId = Long.valueOf(orderIdStr);

                System.out.println("🎉 Webhook: Thanh toán thành công cho order #" + orderId);

                // Cập nhật trạng thái đơn hàng
                orderService.updateOrderStatus(orderId, OrderStatus.PAID);

                // Cập nhật payment
                Payment payment = paymentRepository.findByTransactionId(session.getId());
                if (payment != null) {
                    payment.setStatus(PaymentStatus.COMPLETED);
                    paymentRepository.save(payment);

                    // Tạo bill
                    BillRequest billReq = new BillRequest();
                    billReq.setOrderId(orderId);
                    billReq.setPaymentMethod("STRIPE");
                    Order order = orderService.getOrderById(orderId).get();
                    billReq.setPromotionId(order.getPromotion() != null ? order.getPromotion().getId() : null);
                    billService.createBill(billReq);
                }

                System.out.println("✅ Đã cập nhật thành PAID cho order #" + orderId);
            }

            return ResponseEntity.ok("Webhook processed");
        } catch (Exception e) {
            System.err.println("❌ Webhook error: " + e.getMessage());
            return ResponseEntity.badRequest().body("Webhook error");
        }
    }

    @PostMapping("/stripe/complete/{orderId}")
    public ResponseEntity<?> completeStripePayment(@PathVariable Long orderId) {
        try {
            Order order = orderService.getOrderById(orderId)
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy đơn hàng!"));

            System.out.println("🔄 Đang cập nhật payment status cho order #" + orderId);

            // Tìm payment của order
            Payment payment = order.getPayment();
            if (payment != null && payment.getMethod() == PaymentMethod.STRIPE) {
                payment.setStatus(PaymentStatus.COMPLETED);
                paymentRepository.save(payment);
                System.out.println("✅ Đã cập nhật payment status thành COMPLETED cho order #" + orderId);
            } else {
                System.out.println("⚠️ Không tìm thấy payment Stripe cho order #" + orderId);
            }

            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "Đã cập nhật trạng thái thanh toán!"));

        } catch (Exception e) {
            System.err.println("❌ Lỗi cập nhật payment: " + e.getMessage());
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "Lỗi: " + e.getMessage()));
        }
    }

    @PostMapping("/payos/{orderId}")
    public ResponseEntity<?> payWithPayOS(@PathVariable Long orderId) {
        try {
            Order order = orderService.getOrderById(orderId)
                    .orElseThrow(() -> new RuntimeException("Order not found"));

            Double amount = getPaymentAmount(order);

            // 👉 EP orderCode giống ID đơn hàng trong DB
            long orderCode = orderId;

            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("orderCode", orderCode);
            requestBody.put("amount", amount.intValue());
            requestBody.put("description", "Thanh toán đơn hàng #" + orderId);

            requestBody.put("returnUrl",
                    "http://localhost:3000/online-result?orderId=" + orderId + "&status=PAID");

            requestBody.put("cancelUrl",
                    "http://localhost:3000/payment-cancel?orderId=" + orderId);

            List<Map<String, Object>> items = new ArrayList<>();
            Map<String, Object> item = new HashMap<>();
            item.put("name", "Đơn hàng #" + orderId);
            item.put("quantity", 1);
            item.put("price", amount.intValue());
            items.add(item);
            requestBody.put("items", items);

            String dataForSignature = "amount=" + amount.intValue() +
                    "&cancelUrl=http://localhost:3000/payment-cancel?orderId=" + orderId +
                    "&description=Thanh toán đơn hàng #" + orderId +
                    "&orderCode=" + orderCode +
                    "&returnUrl=http://localhost:3000/online-result?orderId=" + orderId + "&status=PAID";

            String signature = hmacSHA256(dataForSignature, payOSConfig.getChecksumKey());
            requestBody.put("signature", signature);

            HttpHeaders headers = new HttpHeaders();
            headers.set("x-client-id", payOSConfig.getClientId());
            headers.set("x-api-key", payOSConfig.getApiKey());
            headers.setContentType(MediaType.APPLICATION_JSON);

            RestTemplate restTemplate = new RestTemplate();
            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);

            ResponseEntity<Map> response = restTemplate.postForEntity(
                    "https://api-merchant.payos.vn/v2/payment-requests", entity, Map.class);

            Map<String, Object> responseData = (Map<String, Object>) response.getBody().get("data");

            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "payUrl", responseData.get("checkoutUrl")));

        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of(
                    "success", false,
                    "message", e.getMessage()));
        }
    }

    @RequestMapping(value = "/payos/callback", method = { RequestMethod.GET, RequestMethod.POST }, consumes = "*/*")
    public ResponseEntity<?> handlePayOSCallback(@RequestBody(required = false) Map<String, Object> payload) {

        System.out.println("📩 PAYOS CALLBACK => " + payload);

        try {
            if (payload == null || !payload.containsKey("data")) {
                return ResponseEntity.ok(Map.of("success", true, "message", "Callback received"));
            }

            Map<String, Object> data = (Map<String, Object>) payload.get("data");

            Long orderId = Long.valueOf(data.get("orderCode").toString());

            orderService.updateOrderStatus(orderId, OrderStatus.PAID);

            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "✔ Payment verified & database updated!"));

        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of(
                    "success", false,
                    "message", e.getMessage()));
        }
    }

    @GetMapping("/payos/callback")
    public ResponseEntity<?> testWebhook() {
        return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Webhook is working"));
    }

    @PostMapping("/stripe/confirm/{orderId}")

    public ResponseEntity<?> confirmStripePayment(@PathVariable Long orderId) {
        try {
            System.out.println("🎯 Nhận request xác nhận Stripe cho order #" + orderId);

            Order order = orderService.getOrderById(orderId)
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy đơn hàng #" + orderId));

            // Cập nhật trạng thái đơn hàng
            orderService.updateOrderStatus(orderId, OrderStatus.PAID);

            // Cập nhật payment
            Payment payment = order.getPayment();
            if (payment != null) {
                payment.setStatus(PaymentStatus.COMPLETED);
                paymentRepository.save(payment);

                // Tạo bill
                BillRequest billReq = new BillRequest();
                billReq.setOrderId(orderId);
                billReq.setPaymentMethod("STRIPE");
                billReq.setPromotionId(order.getPromotion() != null ? order.getPromotion().getId() : null);
                billService.createBill(billReq);
            }

            System.out.println("✅ Đã cập nhật thành PAID cho order #" + orderId);

            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "✅ Đã xác nhận thanh toán Stripe cho đơn #" + orderId));

        } catch (Exception e) {
            System.err.println("❌ Lỗi xác nhận Stripe: " + e.getMessage());
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "❌ Lỗi xác nhận thanh toán: " + e.getMessage()));
        }
    }
}
