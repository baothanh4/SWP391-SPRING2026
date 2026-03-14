package com.example.SWP391_SPRING2026.DTO.Response;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
public class DashboardResponseDTO {
    private BigDecimal totalRevenue;
    private Long totalOrders;
    private BigDecimal averageOrderValue;
    private Double cancellationRate;

    private List<GenderStatsDTO> genderStats;

    private List<BestSellerDTO> bestSellers;

    private List<LowStockDTO> lowStockProducts;

}
