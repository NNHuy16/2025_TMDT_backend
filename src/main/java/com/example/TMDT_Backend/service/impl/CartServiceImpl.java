package com.example.TMDT_Backend.service.impl;

import com.example.TMDT_Backend.entity.Cart;
import com.example.TMDT_Backend.entity.CartItem;
import com.example.TMDT_Backend.entity.Product;
import com.example.TMDT_Backend.entity.User;
import com.example.TMDT_Backend.repository.CartItemRepository;
import com.example.TMDT_Backend.repository.CartRepository;
import com.example.TMDT_Backend.repository.ProductRepository;
import com.example.TMDT_Backend.repository.UserRepository;
import com.example.TMDT_Backend.service.CartService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CartServiceImpl implements CartService {

    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;

    @Override
    public Optional<Cart> getCartByUserId(Long userId) {
        return cartRepository.findByUserId(userId);
    }

    @Override
    @Transactional
    public Cart createCart(User user) {
        Cart cart = new Cart();
        cart.setUser(user);
        cart.setTotalAmount(0.0);
        return cartRepository.save(cart);
    }

    @Override
    @Transactional
    public Cart addProductToCart(Long userId, Integer productId, Integer quantity) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Product not found"));

        Cart cart = cartRepository.findByUserId(userId).orElseGet(() -> createCart(user));

        Optional<CartItem> existingCartItem = cart.getCartItems().stream()
                .filter(item -> item.getProduct().getId().equals(productId))
                .findFirst();

        if (existingCartItem.isPresent()) {
            CartItem cartItem = existingCartItem.get();
            cartItem.setQuantity(cartItem.getQuantity() + quantity);
            cartItem.setPrice(product.getPrice() * cartItem.getQuantity());
            cartItemRepository.save(cartItem);
        } else {
            CartItem cartItem = new CartItem();
            cartItem.setCart(cart);
            cartItem.setProduct(product);
            cartItem.setQuantity(quantity);
            cartItem.setPrice(product.getPrice() * quantity);
            cartItemRepository.save(cartItem);
            cart.getCartItems().add(cartItem);
        }
        cart.setTotalAmount(calculateCartTotal(userId));
        return cartRepository.save(cart);
    }

    @Override
    @Transactional
    public Cart updateProductQuantityInCart(Long userId, Integer productId, Integer quantity) {
        if (quantity <= 0) {
            removeProductFromCart(userId, productId);
            // Recalculate total after removal
            Cart cart = cartRepository.findByUserId(userId)
                    .orElseThrow(() -> new RuntimeException("Cart not found"));
            cart.setTotalAmount(calculateCartTotal(userId));
            return cartRepository.save(cart);
        }

        Cart cart = cartRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("Cart not found"));

        CartItem cartItem = cart.getCartItems().stream()
                .filter(item -> item.getProduct().getId().equals(productId))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Product not found in cart"));

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Product not found"));

        cartItem.setQuantity(quantity);
        cartItem.setPrice(product.getPrice() * quantity);
        cartItemRepository.save(cartItem);

        cart.setTotalAmount(calculateCartTotal(userId));
        return cartRepository.save(cart);
    }

    @Override
    @Transactional
    public void removeProductFromCart(Long userId, Integer productId) {
        Cart cart = cartRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("Cart not found"));

        CartItem cartItemToRemove = cart.getCartItems().stream()
                .filter(item -> item.getProduct().getId().equals(productId))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Product not found in cart"));

        cart.getCartItems().remove(cartItemToRemove);
        cartItemRepository.delete(cartItemToRemove);

        cart.setTotalAmount(calculateCartTotal(userId));
        cartRepository.save(cart);
    }

    @Override
    @Transactional
    public void clearCart(Long userId) {
        Cart cart = cartRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("Cart not found"));

        cartItemRepository.deleteAll(cart.getCartItems());
        cart.getCartItems().clear();
        cart.setTotalAmount(0.0);
        cartRepository.save(cart);
    }

    @Override
    public Double calculateCartTotal(Long userId) {
        Cart cart = cartRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("Cart not found"));

        return cart.getCartItems().stream()
                .mapToDouble(CartItem::getPrice)
                .sum();
    }
} 