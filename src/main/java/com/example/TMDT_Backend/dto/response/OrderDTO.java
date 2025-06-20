package com.example.TMDT_Backend.dto.response;

import com.example.TMDT_Backend.entity.enums.PaymentStatus;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class OrderDTO {
    private String txnRef;
    private UserDTO user; // Hoặc userId nếu không muốn trả nested object
    private int amount;
    private PaymentStatus status;
    private String paymentMethod;
    private LocalDateTime createdAt;

    private List<OrderDetailDTO> orderDetails;
}
