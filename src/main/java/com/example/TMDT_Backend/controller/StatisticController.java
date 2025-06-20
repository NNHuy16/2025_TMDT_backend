package com.example.TMDT_Backend.controller;

import com.example.TMDT_Backend.dto.response.RevenueReportDTO;
import com.example.TMDT_Backend.service.StatisticService;
import com.example.TMDT_Backend.service.UserService;
import com.example.TMDT_Backend.security.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/statistics")
@RequiredArgsConstructor
public class StatisticController {

    private final StatisticService statisticService;
    private final UserService userService;

    @GetMapping("/revenue-by-date-range")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<RevenueReportDTO> getRevenueReportByDateRange(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        return ResponseEntity.ok(statisticService.getRevenueReportByDateRange(startDate, endDate));
    }

    @GetMapping("/monthly-revenue-by-category")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<RevenueReportDTO> getMonthlyRevenueByCategory(
            @RequestParam int year,
            @RequestParam int month) {
        return ResponseEntity.ok(statisticService.getMonthlyRevenueByCategory(year, month));
    }

    @GetMapping("/seller/revenue-by-date-range")
    @PreAuthorize("hasRole('SELLER')")
    public ResponseEntity<RevenueReportDTO> getSellerRevenueReportByDateRange(
            @AuthenticationPrincipal CustomUserDetails currentUser,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        if (currentUser == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        return ResponseEntity.ok(statisticService.getSellerRevenueReportByDateRange(currentUser.getId(), startDate, endDate));
    }

    @GetMapping("/seller/monthly-revenue-by-category")
    @PreAuthorize("hasRole('SELLER')")
    public ResponseEntity<RevenueReportDTO> getSellerMonthlyRevenueByCategory(
            @AuthenticationPrincipal CustomUserDetails currentUser,
            @RequestParam int year,
            @RequestParam int month) {
        if (currentUser == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        return ResponseEntity.ok(statisticService.getSellerMonthlyRevenueByCategory(currentUser.getId(), year, month));
    }
} 