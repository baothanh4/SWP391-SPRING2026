package com.example.SWP391_SPRING2026.Service;


import com.example.SWP391_SPRING2026.DTO.Response.BestSellerDTO;
import com.example.SWP391_SPRING2026.DTO.Response.DashboardResponseDTO;
import com.example.SWP391_SPRING2026.DTO.Response.GenderStatsDTO;
import com.example.SWP391_SPRING2026.DTO.Response.LowStockDTO;
import com.example.SWP391_SPRING2026.Repository.OrderItemRepository;
import com.example.SWP391_SPRING2026.Repository.OrderRepository;
import com.example.SWP391_SPRING2026.Repository.ProductVariantRepository;
import com.example.SWP391_SPRING2026.Repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class DashboardService {

    private final OrderRepository orderRepository;
    private final UserRepository usersRepository;
    private final ProductVariantRepository variantRepository;
    private final OrderItemRepository orderItemRepository;

    public DashboardResponseDTO getDashboard() {

        BigDecimal totalRevenue = orderRepository.getTotalRevenue();
        Long totalOrders = orderRepository.getTotalOrders();
        BigDecimal aov = orderRepository.getAverageOrderValue();
        Double cancelRate = orderRepository.getCancellationRate();

        List<BestSellerDTO> bestSellers =
                orderItemRepository.findBestSellers(PageRequest.of(0,10));

        List<LowStockDTO> lowStock =
                variantRepository.findLowStockProducts(10);

        List<Object[]> genderRaw = usersRepository.getGenderStats();
        Long totalUsers = usersRepository.countAllUsers();

        List<GenderStatsDTO> genderStats = new ArrayList<>();

        for(Object[] row : genderRaw){

            Integer gender = (Integer) row[0];
            Long count = (Long) row[1];

            double percent = (count * 100.0) / totalUsers;

            genderStats.add(
                    new GenderStatsDTO(gender,count,percent)
            );
        }

        return DashboardResponseDTO.builder()
                .totalRevenue(totalRevenue)
                .totalOrders(totalOrders)
                .averageOrderValue(aov)
                .cancellationRate(cancelRate)
                .bestSellers(bestSellers)
                .lowStockProducts(lowStock)
                .genderStats(genderStats)
                .build();
    }
}
