package com.example.TMDT_Backend.service;

import com.example.TMDT_Backend.entity.Cart;
import com.example.TMDT_Backend.entity.CartItem;
import com.example.TMDT_Backend.entity.Product;
import com.example.TMDT_Backend.entity.User;

import java.util.List;
import java.util.Optional;

public interface CartService {
    Optional<Cart> getCartByUserId(Long userId);
    Cart createCart(User user);
    Cart addProductToCart(Long userId, Integer productId, Integer quantity);
    Cart updateProductQuantityInCart(Long userId, Integer productId, Integer quantity);
    void removeProductFromCart(Long userId, Integer productId);
    void clearCart(Long userId);
    Double calculateCartTotal(Long userId);
} 