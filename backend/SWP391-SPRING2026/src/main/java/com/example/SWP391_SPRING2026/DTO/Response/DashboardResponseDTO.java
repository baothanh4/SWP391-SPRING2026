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

    private List<RevenueTimeDTO> revenueByMonth;
    private List<RevenueTimeDTO> revenueByQuarter;

    private List<SimpleStatDTO> paymentStats;
    private List<SimpleStatDTO> orderStats;

    private List<BestSellerDTO> bestSellers;
    private List<LowStockDTO> lowStockProducts;

}
