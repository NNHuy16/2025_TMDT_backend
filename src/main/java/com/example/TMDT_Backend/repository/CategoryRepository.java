package com.example.TMDT_Backend.repository;

import com.example.TMDT_Backend.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
 
@Repository
public interface CategoryRepository extends JpaRepository<Category, Integer> {
} 