package com.example.TMDT_Backend.repository;

import com.example.TMDT_Backend.entity.OrderDetail;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

@Repository
public interface OrderDetailRepository extends JpaRepository<OrderDetail, Integer> {
    @Query("SELECT od FROM OrderDetail od WHERE od.order.order_ID = :orderId")
    List<OrderDetail> findByOrderId(Integer orderId);
} 