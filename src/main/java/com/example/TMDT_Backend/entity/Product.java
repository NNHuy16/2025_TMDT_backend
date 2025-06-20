package com.example.TMDT_Backend.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@NoArgsConstructor
@Table(name = "products")
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    private String description;

    private int price; // Giá hiện tại

    private int stockQuantity; // Số lượng tồn kho

    private String imageUrl; // Đường dẫn ảnh sản phẩm
}
