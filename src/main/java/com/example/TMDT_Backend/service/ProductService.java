package com.example.TMDT_Backend.service;

import com.example.TMDT_Backend.entity.Product;
import com.example.TMDT_Backend.entity.Category;

import java.util.List;
import java.util.Optional;

public interface ProductService {
    List<Product> findAll();
    Optional<Product> findById(Integer id);
    Product save(Product product);
    void deleteById(Integer id);
    List<Product> findByCategoryId(Integer categoryId);
    List<Product> findBySellerId(Integer sellerId);
} 