package com.example.TMDT_Backend.controller;

import com.example.TMDT_Backend.entity.Voucher;
import com.example.TMDT_Backend.service.VoucherService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/vouchers")
@RequiredArgsConstructor
public class VoucherController {

    private final VoucherService voucherService;

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<Voucher>> getAllVouchers() {
        return ResponseEntity.ok(voucherService.findAll());
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Voucher> getVoucherById(@PathVariable Integer id) {
        return voucherService.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/code/{code}")
    public ResponseEntity<Voucher> getVoucherByCode(@PathVariable String code) {
        return voucherService.findByCode(code)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping
    public ResponseEntity<Voucher> createVoucher(@RequestBody Voucher voucher) {
        return ResponseEntity.ok(voucherService.save(voucher));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{id}")
    public ResponseEntity<Voucher> updateVoucher(@PathVariable Integer id, @RequestBody Voucher voucher) {
        return voucherService.findById(id)
                .map(existingVoucher -> {
                    existingVoucher.setCode(voucher.getCode());
                    existingVoucher.setDiscountAmount(voucher.getDiscountAmount());
                    existingVoucher.setExpiryDate(voucher.getExpiryDate());
                    existingVoucher.setUsageLimit(voucher.getUsageLimit());
                    existingVoucher.setUsedCount(voucher.getUsedCount());
                    existingVoucher.setStatus(voucher.getStatus());
                    return ResponseEntity.ok(voucherService.save(existingVoucher));
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteVoucher(@PathVariable Integer id) {
        if (voucherService.findById(id).isPresent()) {
            voucherService.deleteById(id);
            return ResponseEntity.noContent().build();
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping("/apply")
    public ResponseEntity<Boolean> applyVoucher(@RequestBody Map<String, String> payload) {
        String code = payload.get("code");
        if (code == null) {
            return ResponseEntity.badRequest().body(false);
        }
        boolean applied = voucherService.applyVoucher(code);
        return ResponseEntity.ok(applied);
    }
} 