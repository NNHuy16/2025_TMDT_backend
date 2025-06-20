package com.example.TMDT_Backend.dto.request;

import lombok.Data;

import java.util.List;

@Data
public class CreateOrderRequest {
    private List<OrderItem> items;
    private String bankCode;
    private String language;

    @Data
    public static class OrderItem {
        private Long productId;
        private int quantity;
    }
}
