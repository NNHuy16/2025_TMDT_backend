package com.example.TMDT_Backend.dto.response;

import lombok.Data;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class RevenueReportDTO {
    private String period;
    private double totalRevenue;
    private Map<String, Double> revenueByCategories;
    private List<DailyRevenue>
    dailyRevenue;

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class DailyRevenue {
        private String date;
        private double amount;
    }
} 