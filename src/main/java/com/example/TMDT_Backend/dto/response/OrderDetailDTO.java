package com.example.TMDT_Backend.dto.response;

import lombok.Data;

@Data
public class OrderDetailDTO {
    private Long productId;
    private String productName;
    private int price;
    private int quantity;
}
