package com.example.TMDT_Backend.repository;

import com.example.TMDT_Backend.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

@Repository
public interface ProductRepository extends JpaRepository<Product, Integer> {
    // Có thể thêm các phương thức custom nếu cần, ví dụ:
    // Optional<Product> findByName(String name);
    @Query("SELECT p FROM Product p WHERE p.category.cate_ID = :categoryId")
    List<Product> findByCategory_Cate_ID(Integer categoryId);
    List<Product> findBySeller_Id(Integer sellerId);
}
