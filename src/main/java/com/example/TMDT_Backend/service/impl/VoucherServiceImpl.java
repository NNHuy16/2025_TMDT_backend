package com.example.TMDT_Backend.service.impl;

import com.example.TMDT_Backend.entity.Voucher;
import com.example.TMDT_Backend.repository.VoucherRepository;
import com.example.TMDT_Backend.service.VoucherService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class VoucherServiceImpl implements VoucherService {

    private final VoucherRepository voucherRepository;

    @Override
    public List<Voucher> findAll() {
        return voucherRepository.findAll();
    }

    @Override
    public Optional<Voucher> findById(Integer id) {
        return voucherRepository.findById(id);
    }

    @Override
    public Optional<Voucher> findByCode(String code) {
        return voucherRepository.findByCode(code);
    }

    @Override
    public Voucher save(Voucher voucher) {
        return voucherRepository.save(voucher);
    }

    @Override
    public void deleteById(Integer id) {
        voucherRepository.deleteById(id);
    }

    @Override
    @Transactional
    public boolean applyVoucher(String code) {
        Optional<Voucher> voucherOptional = voucherRepository.findByCode(code);
        if (voucherOptional.isPresent()) {
            Voucher voucher = voucherOptional.get();
            if (voucher.getStatus() == Voucher.Status.active &&
                voucher.getExpiryDate().isAfter(LocalDateTime.now()) &&
                voucher.getUsedCount() < voucher.getUsageLimit()) {
                
                voucher.setUsedCount(voucher.getUsedCount() + 1);
                voucherRepository.save(voucher);
                return true;
            }
        }
        return false;
    }
} 