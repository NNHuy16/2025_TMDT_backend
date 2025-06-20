package com.example.TMDT_Backend.controller;

import com.example.TMDT_Backend.config.VNPayConfig;
//import com.example.TMDT_Backend.entity.Order;
import com.example.TMDT_Backend.dto.request.CreateOrderRequest;
import com.example.TMDT_Backend.entity.Order;
import com.example.TMDT_Backend.entity.OrderDetail;
import com.example.TMDT_Backend.entity.Product;
import com.example.TMDT_Backend.entity.User;
import com.example.TMDT_Backend.entity.enums.PaymentStatus;
import com.example.TMDT_Backend.entity.enums.Role;
import com.example.TMDT_Backend.repository.OrderRepository;
import com.example.TMDT_Backend.repository.ProductRepository;
import com.example.TMDT_Backend.repository.UserRepository;
import com.example.TMDT_Backend.security.CustomUserDetails;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.view.RedirectView;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.security.Principal;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;


@RestController
@RequestMapping("/api/vnpay")
public class VNPayController {

    private final OrderRepository orderRepository;
    private final UserRepository userRepository;
    private final ProductRepository productRepository;


    public VNPayController(OrderRepository orderRepository, OrderRepository orderRepository1, UserRepository userRepository, ProductRepository productRepository) {
        this.orderRepository = orderRepository;
        this.userRepository = userRepository;
        this.productRepository = productRepository;
    }

    @PostMapping("/create_payment_url")
    @PreAuthorize("hasRole('USER')")
    public Map<String, Object> createPaymentUrl(
            @RequestBody CreateOrderRequest requestBody,
            HttpServletRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) throws Exception {
        List<CreateOrderRequest.OrderItem> items = requestBody.getItems();
        String bankCode = requestBody.getBankCode();
        String language = requestBody.getLanguage() != null ? requestBody.getLanguage() : "vn";

        if (items == null || items.isEmpty()) {
            throw new IllegalArgumentException("Giỏ hàng không được để trống");
        }

        int totalAmount = 0;
        List<OrderDetail> orderDetails = new ArrayList<>();

        for (CreateOrderRequest.OrderItem item : items) {
            Product product = productRepository.findById(item.getProductId())
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy sản phẩm ID: " + item.getProductId()));

            int price = product.getPrice();
            int quantity = item.getQuantity();
            if (quantity <= 0) throw new IllegalArgumentException("Số lượng không hợp lệ");

            totalAmount += price * quantity;

            OrderDetail detail = new OrderDetail();
            detail.setProduct(product);
            detail.setQuantity(quantity);
            detail.setPrice(price);
            orderDetails.add(detail);
        }

        String vnp_TxnRef = VNPayConfig.getRandomNumber(8);
        User user = userDetails.getUser();

        Order order = new Order();
        order.setTxnRef(vnp_TxnRef);
        order.setAmount(totalAmount);
        order.setStatus(PaymentStatus.PENDING);
        order.setUser(user);
        order.setCreatedAt(LocalDateTime.now());

        for (OrderDetail detail : orderDetails) {
            detail.setOrder(order);
        }
        order.setOrderDetails(orderDetails);

        orderRepository.save(order);

        // Cấu hình VNPAY
        String vnp_Version = "2.1.0";
        String vnp_Command = "pay";
        String orderType = "other";
        long vnp_Amount = totalAmount * 100L;
        String vnp_IpAddr = VNPayConfig.getIpAddress(request);
        String vnp_TmnCode = VNPayConfig.vnp_TmnCode;

        // Cấu hình các tham số gửi tới VNPAY
        Map<String, String> vnp_Params = new HashMap<>();
        vnp_Params.put("vnp_Version", vnp_Version);
        vnp_Params.put("vnp_Command", vnp_Command);
        vnp_Params.put("vnp_TmnCode", vnp_TmnCode);
        vnp_Params.put("vnp_Amount", String.valueOf(vnp_Amount));
        vnp_Params.put("vnp_CurrCode", "VND");
        if (bankCode != null && !bankCode.isEmpty()) {
            vnp_Params.put("vnp_BankCode", bankCode);
        }
        vnp_Params.put("vnp_TxnRef", vnp_TxnRef);
        vnp_Params.put("vnp_OrderInfo", "Thanh toan don hang: " + vnp_TxnRef);
        vnp_Params.put("vnp_OrderType", orderType);
        vnp_Params.put("vnp_Locale", language);
        vnp_Params.put("vnp_ReturnUrl", VNPayConfig.vnp_ReturnUrl);
        vnp_Params.put("vnp_IpAddr", vnp_IpAddr);

        Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("Etc/GMT+7"));
        SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMddHHmmss");
        String vnp_CreateDate = formatter.format(cal.getTime());
        vnp_Params.put("vnp_CreateDate", vnp_CreateDate);
        cal.add(Calendar.MINUTE, 15);
        String vnp_ExpireDate = formatter.format(cal.getTime());
        vnp_Params.put("vnp_ExpireDate", vnp_ExpireDate);

        // Sinh chuỗi query và hash
        List<String> fieldNames = new ArrayList<>(vnp_Params.keySet());
        Collections.sort(fieldNames);
        StringBuilder hashData = new StringBuilder();
        StringBuilder query = new StringBuilder();
        for (String fieldName : fieldNames) {
            String fieldValue = vnp_Params.get(fieldName);
            if (fieldValue != null && !fieldValue.isEmpty()) {
                hashData.append(fieldName).append('=').append(URLEncoder.encode(fieldValue, StandardCharsets.US_ASCII));
                query.append(URLEncoder.encode(fieldName, StandardCharsets.US_ASCII))
                        .append('=')
                        .append(URLEncoder.encode(fieldValue, StandardCharsets.US_ASCII));
                if (!fieldName.equals(fieldNames.get(fieldNames.size() - 1))) {
                    hashData.append('&');
                    query.append('&');
                }
            }
        }

        String vnp_SecureHash = VNPayConfig.hmacSHA512(VNPayConfig.secretKey, hashData.toString());
        query.append("&vnp_SecureHash=").append(vnp_SecureHash);
        String paymentUrl = VNPayConfig.vnp_PayUrl + "?" + query;

        Map<String, Object> res = new HashMap<>();
        res.put("code", "00");
        res.put("message", "success");
        res.put("data", paymentUrl);
        return res;
    }

    /**
     * API xử lý khi VNPAY redirect về hệ thống với kết quả thanh toán.
     * Hệ thống xác minh chuỗi hash, cập nhật trạng thái đơn hàng.
     *
     * @param request Chứa tất cả tham số từ VNPAY.
     * @return Thông báo kết quả giao dịch.
     */
//    @PreAuthorize("hasRole('USER')")
//    @GetMapping("/vnpay-return")
//    public ResponseEntity<String> vnpayReturn(HttpServletRequest request) {
//        Map<String, String> fields = new HashMap<>();
//        for (Enumeration<String> params = request.getParameterNames(); params.hasMoreElements(); ) {
//            String paramName = params.nextElement();
//            fields.put(paramName, request.getParameter(paramName));
//        }
//
//        String vnp_SecureHash = fields.remove("vnp_SecureHash");
//        String generatedHash = VNPayConfig.hashAllFields(fields);
//
//        if (vnp_SecureHash != null && vnp_SecureHash.equals(generatedHash)) {
//            String responseCode = fields.get("vnp_ResponseCode");
//            String txnRef = fields.get("vnp_TxnRef");
//
//            MembershipOrder order = membershipOrderRepository.findByTxnRef(txnRef).orElse(null);
//            if (order == null) {
//                return ResponseEntity.status(404).body("Đơn hàng không tồn tại");
//            }
//
//            if ("00".equals(responseCode)) {
//                order.setStatus(PaymentStatus.PENDING); // Đã thanh toán, chờ admin duyệt
//                membershipOrderRepository.save(order);
//
//                return ResponseEntity.ok("Thanh toán thành công. Vui lòng đợi Admin duyệt gói.");
//            } else {
//                order.setStatus(PaymentStatus.FAILED);
//                membershipOrderRepository.save(order);
//                return ResponseEntity.status(400).body("Thanh toán thất bại. Mã lỗi: " + responseCode);
//            }
//        } else {
//            return ResponseEntity.status(403).body("Chuỗi kiểm tra sai. Không xác thực được nguồn gửi.");
//        }
//}
//
    @GetMapping("/vnpay-return")
    public RedirectView vnpayReturn(HttpServletRequest request) {
        Map<String, String> fields = new HashMap<>();
        for (Enumeration<String> params = request.getParameterNames(); params.hasMoreElements(); ) {
            String paramName = params.nextElement();
            fields.put(paramName, request.getParameter(paramName));
        }

        String vnp_SecureHash = fields.remove("vnp_SecureHash");
        String generatedHash = VNPayConfig.hashAllFields(fields);

        String status = "fail";
        String message = "Thanh toán thất bại.";

        if (vnp_SecureHash != null && vnp_SecureHash.equals(generatedHash)) {
            String responseCode = fields.get("vnp_ResponseCode");
            String txnRef = fields.get("vnp_TxnRef");

            Order order = orderRepository.findByTxnRef(txnRef).orElse(null);
            if (order == null) {
                message = "Đơn hàng không tồn tại.";
            } else if ("00".equals(responseCode)) {
                order.setStatus(PaymentStatus.SUCCESS);
                orderRepository.save(order);

                status = "success";
                message = "Thanh toán thành công!";
            } else {
                order.setStatus(PaymentStatus.FAILED);
                orderRepository.save(order);
                message = "Thanh toán thất bại. Mã lỗi: " + responseCode;
            }
        } else {
            message = "Chuỗi kiểm tra không hợp lệ.";
        }

        String redirectUrl = "http://localhost:3000/..."
                + "?paymentStatus=" + status
                + "&message=" + URLEncoder.encode(message, StandardCharsets.UTF_8);

        return new RedirectView(redirectUrl);
    }

}
