package com.example.TMDT_Backend.repository;

import com.example.TMDT_Backend.entity.Order;
import com.example.TMDT_Backend.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface OrderRepository extends JpaRepository<Order, String> {
    List<Order> findByUser(User user);
    Optional<Order> findByTxnRef(String txnRef);
    List<Order> findByUser_Id(Long userId);



}
