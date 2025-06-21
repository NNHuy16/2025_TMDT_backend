package com.example.TMDT_Backend.controller;

import com.example.TMDT_Backend.entity.Cart;
import com.example.TMDT_Backend.service.CartService;
// import com.example.TMDT_Backend.service.UserService; // Not directly used in this controller anymore
import com.example.TMDT_Backend.security.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/carts")
@RequiredArgsConstructor
public class CartController {

    private final CartService cartService;

    @PostMapping("/add")
    @PreAuthorize("hasAnyRole('ADMIN', 'USER', 'SELLER')")
    public ResponseEntity<Object> addProductToCart(@AuthenticationPrincipal CustomUserDetails currentUser, @RequestBody Map<String, Integer> payload) {
        if (currentUser == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("User not authenticated");
        }
        Integer productId = payload.get("productId");
        Integer quantity = payload.get("quantity");

        if (productId == null || quantity == null || quantity <= 0) {
            return ResponseEntity.badRequest().body("Product ID and quantity must be provided and quantity must be positive.");
        }

        Cart updatedCart = cartService.addProductToCart(currentUser.getId(), productId, quantity);
        return ResponseEntity.ok(updatedCart);
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'USER', 'SELLER')")
    public ResponseEntity<Object> getCartByUserId(@AuthenticationPrincipal CustomUserDetails currentUser) {
        if (currentUser == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("User not authenticated");
        }
        Cart cart = cartService.getCartByUserId(currentUser.getId()).orElse(null); // Changed to orElse(null)
        if (cart == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Cart not found for this user.");
        }
        return ResponseEntity.ok(cart);
    }

    @PutMapping("/update")
    @PreAuthorize("hasAnyRole('ADMIN', 'USER', 'SELLER')")
    public ResponseEntity<Object> updateCartItemQuantity(@AuthenticationPrincipal CustomUserDetails currentUser, @RequestBody Map<String, Integer> payload) {
        if (currentUser == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("User not authenticated");
        }
        Integer productId = payload.get("productId");
        Integer quantity = payload.get("quantity");

        if (productId == null || quantity == null) {
            return ResponseEntity.badRequest().body("Product ID and quantity must be provided.");
        }

        Cart updatedCart = cartService.updateProductQuantityInCart(currentUser.getId(), productId, quantity);
        return ResponseEntity.ok(updatedCart);
    }

    @DeleteMapping("/remove/{productId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'USER', 'SELLER')")
    public ResponseEntity<Object> removeProductFromCart(@AuthenticationPrincipal CustomUserDetails currentUser, @PathVariable Integer productId) {
        if (currentUser == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("User not authenticated");
        }
        cartService.removeProductFromCart(currentUser.getId(), productId);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/clear")
    @PreAuthorize("hasAnyRole('ADMIN', 'USER', 'SELLER')")
    public ResponseEntity<Object> clearCart(@AuthenticationPrincipal CustomUserDetails currentUser) {
        if (currentUser == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("User not authenticated");
        }
        cartService.clearCart(currentUser.getId());
        return ResponseEntity.noContent().build();
    }
} 