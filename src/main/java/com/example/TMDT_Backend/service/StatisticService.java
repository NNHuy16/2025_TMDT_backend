package com.example.TMDT_Backend.service;

import com.example.TMDT_Backend.dto.response.RevenueReportDTO;

import java.time.LocalDate;

public interface StatisticService {
    RevenueReportDTO getRevenueReportByDateRange(LocalDate startDate, LocalDate endDate);
    RevenueReportDTO getMonthlyRevenueByCategory(int year, int month);
    RevenueReportDTO getSellerRevenueReportByDateRange(Long sellerId, LocalDate startDate, LocalDate endDate);
    RevenueReportDTO getSellerMonthlyRevenueByCategory(Long sellerId, int year, int month);
} 