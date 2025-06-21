package com.example.TMDT_Backend.service.impl;

import com.example.TMDT_Backend.dto.response.RevenueReportDTO;
import com.example.TMDT_Backend.entity.Order;
import com.example.TMDT_Backend.repository.OrderRepository;
import com.example.TMDT_Backend.service.StatisticService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class StatisticServiceImpl implements StatisticService {

    private final OrderRepository orderRepository;

    @Override
    public RevenueReportDTO getRevenueReportByDateRange(LocalDate startDate, LocalDate endDate) {
        LocalDateTime startDateTime = startDate.atStartOfDay();
        LocalDateTime endDateTime = endDate.atTime(LocalTime.MAX);

        List<Order> orders = orderRepository.findAll().stream()
                .filter(order -> order.getCreateAt().isAfter(startDateTime) && order.getCreateAt().isBefore(endDateTime))
                .collect(Collectors.toList());

        double totalRevenue = orders.stream()
                .mapToDouble(Order::getTotalAmount)
                .sum();

        List<RevenueReportDTO.DailyRevenue> dailyRevenues = orders.stream()
                .collect(Collectors.groupingBy(
                        order -> order.getCreateAt().toLocalDate(),
                        Collectors.summingDouble(Order::getTotalAmount)
                ))
                .entrySet().stream()
                .map(entry -> new RevenueReportDTO.DailyRevenue(entry.getKey().toString(), entry.getValue()))
                .sorted( (d1, d2) -> d1.getDate().compareTo(d2.getDate()))
                .collect(Collectors.toList());

        return new RevenueReportDTO(
                "Từ " + startDate.toString() + " đến " + endDate.toString(),
                totalRevenue,
                null, // Not grouping by category for this report
                dailyRevenues
        );
    }

    @Override
    public RevenueReportDTO getMonthlyRevenueByCategory(int year, int month) {
        LocalDateTime startOfMonth = LocalDateTime.of(year, month, 1, 0, 0);
        LocalDateTime endOfMonth = startOfMonth.withDayOfMonth(startOfMonth.toLocalDate().lengthOfMonth()).with(LocalTime.MAX);

        List<Order> orders = orderRepository.findAll().stream()
                .filter(order -> order.getCreateAt().isAfter(startOfMonth) && order.getCreateAt().isBefore(endOfMonth))
                .collect(Collectors.toList());

        double totalRevenue = orders.stream()
                .mapToDouble(Order::getTotalAmount)
                .sum();

        Map<String, Double> revenueByCategories = orders.stream()
                .flatMap(order -> order.getOrderDetails().stream())
                .collect(Collectors.groupingBy(
                        orderDetail -> orderDetail.getProduct().getCategory().getName(),
                        Collectors.summingDouble(orderDetail -> orderDetail.getPrice() * orderDetail.getQuantity())
                ));

        return new RevenueReportDTO(
                "Tháng " + month + "/" + year,
                totalRevenue,
                revenueByCategories,
                null // Not grouping by daily revenue for this report
        );
    }

    @Override
    public RevenueReportDTO getSellerRevenueReportByDateRange(Long sellerId, LocalDate startDate, LocalDate endDate) {
        LocalDateTime startDateTime = startDate.atStartOfDay();
        LocalDateTime endDateTime = endDate.atTime(LocalTime.MAX);

        List<Order> orders = orderRepository.findAll().stream()
                .filter(order -> order.getCreateAt().isAfter(startDateTime) && order.getCreateAt().isBefore(endDateTime))
                .filter(order -> order.getOrderDetails().stream()
                        .anyMatch(detail -> detail.getProduct().getSeller().getId().equals(sellerId)))
                .collect(Collectors.toList());

        double totalRevenue = orders.stream()
                .flatMap(order -> order.getOrderDetails().stream())
                .filter(detail -> detail.getProduct().getSeller().getId().equals(sellerId))
                .mapToDouble(detail -> detail.getPrice() * detail.getQuantity())
                .sum();

        List<RevenueReportDTO.DailyRevenue> dailyRevenues = orders.stream()
                .flatMap(order -> order.getOrderDetails().stream()
                        .filter(detail -> detail.getProduct().getSeller().getId().equals(sellerId)))
                .collect(Collectors.groupingBy(
                        orderDetail -> orderDetail.getOrder().getCreateAt().toLocalDate(),
                        Collectors.summingDouble(detail -> detail.getPrice() * detail.getQuantity())
                ))
                .entrySet().stream()
                .map(entry -> new RevenueReportDTO.DailyRevenue(entry.getKey().toString(), entry.getValue()))
                .sorted((d1, d2) -> d1.getDate().compareTo(d2.getDate()))
                .collect(Collectors.toList());

        return new RevenueReportDTO(
                "Doanh thu người bán từ " + startDate.toString() + " đến " + endDate.toString(),
                totalRevenue,
                null,
                dailyRevenues
        );
    }

    @Override
    public RevenueReportDTO getSellerMonthlyRevenueByCategory(Long sellerId, int year, int month) {
        LocalDateTime startOfMonth = LocalDateTime.of(year, month, 1, 0, 0);
        LocalDateTime endOfMonth = startOfMonth.withDayOfMonth(startOfMonth.toLocalDate().lengthOfMonth()).with(LocalTime.MAX);

        List<Order> orders = orderRepository.findAll().stream()
                .filter(order -> order.getCreateAt().isAfter(startOfMonth) && order.getCreateAt().isBefore(endOfMonth))
                .filter(order -> order.getOrderDetails().stream()
                        .anyMatch(detail -> detail.getProduct().getSeller().getId().equals(sellerId)))
                .collect(Collectors.toList());

        double totalRevenue = orders.stream()
                .flatMap(order -> order.getOrderDetails().stream())
                .filter(detail -> detail.getProduct().getSeller().getId().equals(sellerId))
                .mapToDouble(detail -> detail.getPrice() * detail.getQuantity())
                .sum();

        Map<String, Double> revenueByCategories = orders.stream()
                .flatMap(order -> order.getOrderDetails().stream()
                        .filter(detail -> detail.getProduct().getSeller().getId().equals(sellerId)))
                .collect(Collectors.groupingBy(
                        orderDetail -> orderDetail.getProduct().getCategory().getName(),
                        Collectors.summingDouble(detail -> detail.getPrice() * detail.getQuantity())
                ));

        return new RevenueReportDTO(
                "Doanh thu người bán tháng " + month + "/" + year,
                totalRevenue,
                revenueByCategories,
                null
        );
    }
} 