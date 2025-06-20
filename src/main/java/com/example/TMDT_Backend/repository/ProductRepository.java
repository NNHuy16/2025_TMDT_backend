package com.example.TMDT_Backend.repository;

import com.example.TMDT_Backend.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductRepository extends JpaRepository<Product, Long> {
    // Có thể thêm các phương thức custom nếu cần, ví dụ:
    // Optional<Product> findByName(String name);
}
