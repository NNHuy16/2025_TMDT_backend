package com.example.TMDT_Backend.controller;

import com.example.TMDT_Backend.entity.Product;
import com.example.TMDT_Backend.service.ProductService;
import com.example.TMDT_Backend.service.UserService;
import com.example.TMDT_Backend.security.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
// import org.springframework.security.core.userdetails.UserDetails; // No longer needed
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;
    private final UserService userService;

    @GetMapping
    public ResponseEntity<List<Product>> getAllProducts() {
        return ResponseEntity.ok(productService.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Product> getProductById(@PathVariable Integer id) {
        return productService.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/category/{categoryId}")
    public ResponseEntity<List<Product>> getProductsByCategoryId(@PathVariable Integer categoryId) {
        return ResponseEntity.ok(productService.findByCategoryId(categoryId));
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'SELLER')")
    @PostMapping
    public ResponseEntity<Product> createProduct(@RequestBody Product product, @AuthenticationPrincipal CustomUserDetails currentUser) {
        if (currentUser == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        product.setSeller(userService.getUserById(currentUser.getId()).orElseThrow(() -> new RuntimeException("User not found")));
        return ResponseEntity.status(HttpStatus.CREATED).body(productService.save(product));
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'SELLER')")
    @PutMapping("/{id}")
    public ResponseEntity<Product> updateProduct(@PathVariable Integer id, @RequestBody Product product, @AuthenticationPrincipal CustomUserDetails currentUser) {
        if (currentUser == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        Optional<Product> existingProductOptional = productService.findById(id);
        if (existingProductOptional.isPresent()) {
            Product existingProduct = existingProductOptional.get();
            // Allow ADMIN to update any product, or SELLER to update their own products
            if (currentUser.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN")) ||
                (currentUser.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_SELLER")) &&
                 existingProduct.getSeller().getId().equals(currentUser.getId()))) {

                existingProduct.setName(product.getName());
                existingProduct.setDescription(product.getDescription());
                existingProduct.setPrice(product.getPrice());
                existingProduct.setStockQuantity(product.getStockQuantity());
                existingProduct.setImageUrl(product.getImageUrl());
                existingProduct.setCategory(product.getCategory());
                existingProduct.setStatus(product.getStatus());
                // Seller cannot change the seller of a product they don't own, admin can.
                if (currentUser.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"))) {
                    existingProduct.setSeller(product.getSeller());
                }
                return ResponseEntity.ok(productService.save(existingProduct));
            } else {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build(); // Forbidden
            }
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'SELLER')")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProduct(@PathVariable Integer id, @AuthenticationPrincipal CustomUserDetails currentUser) {
        if (currentUser == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        Optional<Product> productOptional = productService.findById(id);
        if (productOptional.isPresent()) {
            Product product = productOptional.get();
            if (currentUser.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN")) ||
                (currentUser.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_SELLER")) &&
                 product.getSeller().getId().equals(currentUser.getId()))) {
                productService.deleteById(id);
                return ResponseEntity.noContent().build();
            } else {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build(); // Forbidden
            }
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    // Seller-specific endpoints
    @GetMapping("/seller")
    @PreAuthorize("hasRole('SELLER')")
    public ResponseEntity<List<Product>> getProductsBySeller(@AuthenticationPrincipal CustomUserDetails currentUser) {
        if (currentUser == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        return ResponseEntity.ok(productService.findBySellerId(currentUser.getId().intValue())); // Cast Long to Integer
    }

    // This is already covered by the general updateProduct, but added for explicit seller control
    @PutMapping("/seller/{id}")
    @PreAuthorize("hasRole('SELLER')")
    public ResponseEntity<Product> updateSellerProduct(@PathVariable Integer id, @RequestBody Product product, @AuthenticationPrincipal CustomUserDetails currentUser) {
        if (currentUser == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        Optional<Product> existingProductOptional = productService.findById(id);
        if (existingProductOptional.isPresent()) {
            Product existingProduct = existingProductOptional.get();
            if (existingProduct.getSeller().getId().equals(currentUser.getId())) {
                existingProduct.setName(product.getName());
                existingProduct.setDescription(product.getDescription());
                existingProduct.setPrice(product.getPrice());
                existingProduct.setStockQuantity(product.getStockQuantity());
                existingProduct.setImageUrl(product.getImageUrl());
                existingProduct.setCategory(product.getCategory());
                existingProduct.setStatus(product.getStatus());
                // Seller cannot change the seller of their own product
                return ResponseEntity.ok(productService.save(existingProduct));
            } else {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build(); // Forbidden: Not the seller's product
            }
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    // This is already covered by the general deleteProduct, but added for explicit seller control
    @DeleteMapping("/seller/{id}")
    @PreAuthorize("hasRole('SELLER')")
    public ResponseEntity<Void> deleteSellerProduct(@PathVariable Integer id, @AuthenticationPrincipal CustomUserDetails currentUser) {
        if (currentUser == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        Optional<Product> productOptional = productService.findById(id);
        if (productOptional.isPresent()) {
            Product product = productOptional.get();
            if (product.getSeller().getId().equals(currentUser.getId())) {
                productService.deleteById(id);
                return ResponseEntity.noContent().build();
            } else {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build(); // Forbidden: Not the seller's product
            }
        } else {
            return ResponseEntity.notFound().build();
        }
    }
} 