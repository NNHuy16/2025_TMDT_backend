package com.example.TMDT_Backend.service;

import com.example.TMDT_Backend.entity.Voucher;

import java.util.List;
import java.util.Optional;

public interface VoucherService {
    List<Voucher> findAll();
    Optional<Voucher> findById(Integer id);
    Optional<Voucher> findByCode(String code);
    Voucher save(Voucher voucher);
    void deleteById(Integer id);
    boolean applyVoucher(String code);
} 