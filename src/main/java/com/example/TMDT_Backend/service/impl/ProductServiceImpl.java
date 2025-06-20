package com.example.TMDT_Backend.service.impl;

import com.example.TMDT_Backend.entity.Product;
import com.example.TMDT_Backend.repository.ProductRepository;
import com.example.TMDT_Backend.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepository;

    @Override
    public List<Product> findAll() {
        return productRepository.findAll();
    }

    @Override
    public Optional<Product> findById(Integer id) {
        return productRepository.findById(id);
    }

    @Override
    public Product save(Product product) {
        return productRepository.save(product);
    }

    @Override
    public void deleteById(Integer id) {
        productRepository.deleteById(id);
    }

    @Override
    public List<Product> findByCategoryId(Integer categoryId) {
        // Assuming you add a method in ProductRepository like:
        // List<Product> findByCategory_Cate_ID(Integer cate_ID);
        return productRepository.findByCategory_Cate_ID(categoryId);
    }

    @Override
    public List<Product> findBySellerId(Integer sellerId) {
        return productRepository.findBySeller_Id(sellerId);
    }
} 