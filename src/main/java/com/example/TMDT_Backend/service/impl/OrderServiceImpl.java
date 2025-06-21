package com.example.TMDT_Backend.service.impl;

import com.example.TMDT_Backend.entity.Order;
import com.example.TMDT_Backend.entity.OrderDetail;
import com.example.TMDT_Backend.entity.CartItem;
import com.example.TMDT_Backend.entity.Cart;
import com.example.TMDT_Backend.repository.OrderRepository;
import com.example.TMDT_Backend.repository.OrderDetailRepository;
import com.example.TMDT_Backend.repository.CartRepository;
import com.example.TMDT_Backend.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;
    private final OrderDetailRepository orderDetailRepository;
    private final CartRepository cartRepository;

    @Override
    public List<Order> findAllOrders() {
        return orderRepository.findAll();
    }

    @Override
    public Optional<Order> findOrderById(Integer id) {
        return orderRepository.findById(id);
    }

    @Override
    @Transactional
    public Order createOrder(Order order) {
        // Save the order first to get an ID for order details
        Order savedOrder = orderRepository.save(order);

        // Create order details from existing cart items if applicable
        if (order.getUser() != null) {
            Optional<Cart> userCart = cartRepository.findByUserId(order.getUser().getId());
            if (userCart.isPresent()) {
                List<OrderDetail> orderDetails = userCart.get().getCartItems().stream()
                        .map(cartItem -> {
                            OrderDetail orderDetail = new OrderDetail();
                            orderDetail.setOrder(savedOrder);
                            orderDetail.setProduct(cartItem.getProduct());
                            orderDetail.setQuantity(cartItem.getQuantity());
                            orderDetail.setPrice(cartItem.getProduct().getPrice()); // Price at the time of order
                            return orderDetail;
                        })
                        .collect(Collectors.toList());
                orderDetailRepository.saveAll(orderDetails);
                savedOrder.setOrderDetails(orderDetails);
            }
        }
        return savedOrder;
    }

    @Override
    @Transactional
    public Order updateOrder(Integer id, Order updatedOrder) {
        return orderRepository.findById(id)
                .map(existingOrder -> {
                    existingOrder.setUser(updatedOrder.getUser());
                    existingOrder.setTotalAmount(updatedOrder.getTotalAmount());
                    existingOrder.setShippingAddress(updatedOrder.getShippingAddress());
                    existingOrder.setPhoneNumber(updatedOrder.getPhoneNumber());
                    existingOrder.setStatus(updatedOrder.getStatus());
                    existingOrder.setTxnRef(updatedOrder.getTxnRef()); // Update txnRef if needed
                    return orderRepository.save(existingOrder);
                })
                .orElseThrow(() -> new RuntimeException("Order not found"));
    }

    @Override
    public void deleteOrder(Integer id) {
        orderRepository.deleteById(id);
    }

    @Override
    public List<Order> findOrdersByUserId(Long userId) {
        return orderRepository.findByUserId(userId);
    }

    @Override
    public Optional<Order> findOrderByTxnRef(String txnRef) {
        return orderRepository.findByTxnRef(txnRef);
    }

    @Override
    public List<Order> findOrdersBySellerId(Long sellerId) {
        // Fetch all orders and then filter by sellerId within their order details
        return orderRepository.findAll().stream()
                .filter(order -> order.getOrderDetails().stream()
                        .anyMatch(orderDetail -> orderDetail.getProduct().getSeller().getId().equals(sellerId)))
                .collect(Collectors.toList());
    }
} 