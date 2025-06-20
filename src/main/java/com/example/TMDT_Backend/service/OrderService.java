package com.example.TMDT_Backend.service;

import com.example.TMDT_Backend.entity.Order;
import com.example.TMDT_Backend.entity.User;

import java.util.List;
import java.util.Optional;

public interface OrderService {
    List<Order> findAllOrders();
    Optional<Order> findOrderById(Integer id);
    Order createOrder(Order order);
    Order updateOrder(Integer id, Order order);
    void deleteOrder(Integer id);
    List<Order> findOrdersByUserId(Long userId);
    Optional<Order> findOrderByTxnRef(String txnRef);
    List<Order> findOrdersBySellerId(Long sellerId);
} 