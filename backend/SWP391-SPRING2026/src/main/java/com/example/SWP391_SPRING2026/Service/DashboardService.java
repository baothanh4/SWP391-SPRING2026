package com.example.SWP391_SPRING2026.Service;


import com.example.SWP391_SPRING2026.DTO.Response.*;
import com.example.SWP391_SPRING2026.Entity.Address;
import com.example.SWP391_SPRING2026.Entity.Order;
import com.example.SWP391_SPRING2026.Entity.Payment;
import com.example.SWP391_SPRING2026.Entity.Shipment;
import com.example.SWP391_SPRING2026.Enum.OrderStatus;
import com.example.SWP391_SPRING2026.Enum.PaymentMethod;
import com.example.SWP391_SPRING2026.Enum.PaymentStatus;
import com.example.SWP391_SPRING2026.Repository.*;
import com.example.SWP391_SPRING2026.mapper.OrderMapper;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
public class DashboardService {

    private final PaymentRepository paymentRepository;
    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final ProductVariantRepository variantRepository;
    private final EntityManager entityManager;

    public DashboardResponseDTO getDashboard(
            LocalDate from,
            LocalDate to
    ) {

        if (from == null) from = LocalDate.now().minusMonths(3);
        if (to == null) to = LocalDate.now();

        // 🔥 convert ở đây
        LocalDateTime fromDateTime = from.atStartOfDay();
        LocalDateTime toDateTime = to.atTime(23, 59, 59);

        BigDecimal revenue = paymentRepository.getRevenue(fromDateTime, toDateTime);

        Long totalOrders = orderRepository.countOrders(fromDateTime, toDateTime);

        Long completedOrders = orderRepository.countCompletedOrders(fromDateTime, toDateTime);

        BigDecimal aov = completedOrders == 0
                ? BigDecimal.ZERO
                : revenue.divide(
                BigDecimal.valueOf(completedOrders),
                2, // số chữ số sau dấu phẩy
                RoundingMode.HALF_UP
        );

        Long total = orderRepository.countAll(fromDateTime, toDateTime);
        Double cancelled = orderRepository.countCancelled(fromDateTime, toDateTime);

        double cancelRate = (total == 0) ? 0 : (cancelled * 100.0 / total);
        cancelRate = Math.round(cancelRate * 100.0) / 100.0;

        List<RevenueTimeDTO> revenueByMonth = paymentRepository
                .getRevenueByMonth(fromDateTime, toDateTime)
                .stream()
                .map(r -> new RevenueTimeDTO(
                        ((Number) r[0]).intValue(),
                        ((Number) r[1]).intValue(),
                        ((Number) r[2]).longValue()
                ))
                .toList();

        List<RevenueTimeDTO> revenueByQuarter = paymentRepository
                .getRevenueByQuarter(fromDateTime, toDateTime)
                .stream()
                .map(r -> new RevenueTimeDTO(
                        ((Number) r[0]).intValue(),
                        ((Number) r[1]).intValue(),
                        ((Number) r[2]).longValue()
                ))
                .toList();

        List<SimpleStatDTO> paymentStats = paymentRepository
                .getPaymentStats(fromDateTime, toDateTime)
                .stream()
                .map(r -> new SimpleStatDTO(
                        r[0].toString(),
                        ((Number) r[1]).longValue()
                ))
                .toList();

        List<SimpleStatDTO> orderStats = orderRepository
                .getOrderStats(fromDateTime, toDateTime)
                .stream()
                .map(r -> new SimpleStatDTO(
                        r[0].toString(),
                        ((Number) r[1]).longValue()
                ))
                .toList();

        return DashboardResponseDTO.builder()
                .totalRevenue(revenue)
                .totalOrders(totalOrders)
                .averageOrderValue(aov)
                .cancellationRate(cancelRate)
                .revenueByMonth(revenueByMonth)
                .revenueByQuarter(revenueByQuarter)
                .paymentStats(paymentStats)
                .orderStats(orderStats)
                .bestSellers(orderItemRepository.findBestSellers(PageRequest.of(0, 10)))
                .lowStockProducts(variantRepository.findLowStockProducts(10))
                .build();
    }

    // 🔥 Revenue detail (drill-down)
    public List<RevenueTimeDTO> getRevenueDetail(
            String type,
            LocalDate from,
            LocalDate to
    ) {

        LocalDateTime fromDateTime = from.atStartOfDay();
        LocalDateTime toDateTime = to.atTime(23, 59, 59);

        String format = switch (type.toLowerCase()) {
            case "month" -> "EXTRACT(MONTH FROM p.paidAt)";
            case "quarter" -> "EXTRACT(QUARTER FROM p.paidAt)";
            case "year" -> "EXTRACT(YEAR FROM p.paidAt)";
            case "day" -> "DATE(p.paidAt)";
            default -> throw new RuntimeException("Invalid type");
        };

        String jpql = """
    SELECT %s, SUM(p.amount)
    FROM Payment p
    WHERE p.status = 'SUCCESS'
    AND p.paidAt BETWEEN :from AND :to
    GROUP BY %s
    ORDER BY %s
""".formatted(format, format, format);

        List<Object[]> raw = entityManager.createQuery(jpql, Object[].class)
                .setParameter("from", fromDateTime)
                .setParameter("to", toDateTime)
                .getResultList();

        return raw.stream()
                .map(r -> new RevenueTimeDTO(
                        Integer.parseInt(r[0].toString()),
                        0,
                        ((Number) r[1]).longValue()
                ))
                .toList();
    }

    // 📦 Order detail
    public Page<OrderResponseDTO> getOrderDetail(
            String status,
            LocalDate from,
            LocalDate to,
            Pageable pageable
    ) {

        LocalDateTime fromDateTime = from.atStartOfDay();
        LocalDateTime toDateTime = to.atTime(23, 59, 59);

        Page<Order> orders;

        if ("ALL".equalsIgnoreCase(status)) {
            orders = orderRepository.findByCreatedAtBetween(
                    fromDateTime, toDateTime, pageable
            );
        } else {
            orders = orderRepository.findByOrderStatusAndCreatedAtBetween(
                    OrderStatus.valueOf(status),
                    fromDateTime,
                    toDateTime,
                    pageable
            );
        }

        return orders.map(this::mapToDTO);
    }

    private OrderResponseDTO mapToDTO(Order o) {

        Shipment shipment = o.getShipment();

        return OrderResponseDTO.builder()
                .id(o.getId())
                .orderCode(o.getOrderCode())
                .orderType(o.getOrderType())
                .orderStatus(o.getOrderStatus())
                .totalAmount(o.getTotalAmount())
                .deposit(o.getDeposit())
                .remainingAmount(o.getRemainingAmount())
                .address(mapAddress(o.getAddress()))
                .createdAt(o.getCreatedAt())
                .paymentMethod(getLatestPaymentMethod(o))
                .paymentStatus(getLatestPaymentStatus(o))
                .ghnOrderCode(shipment != null ? shipment.getGhnOrderCode() : null)
                .shipmentStatus(shipment != null ? shipment.getStatus() : null)
                .approvalStatus(o.getApprovalStatus())
                .supportApprovedAt(o.getSupportApprovedAt())
                .operationConfirmedAt(o.getOperationConfirmedAt())
                .deliveredAt(shipment != null ? shipment.getDeliveredAt() : null)
                .items(OrderMapper.mapItems(o.getOrderItems()))
                .build();
    }

    private AddressResponseDTO mapAddress(Address a) {
        return new AddressResponseDTO(
                a.getId(),
                a.getReceiverName(),
                a.getPhone(),
                a.getAddressLine(),
                a.getWard(),
                a.getDistrict(),
                a.getProvince(),
                a.getIsDefault()
        );
    }

    private PaymentMethod getLatestPaymentMethod(Order o) {
        if (o.getPayments() == null || o.getPayments().isEmpty()) return null;

        return o.getPayments().stream()
                .max(Comparator.comparing(Payment::getCreatedAt))
                .map(Payment::getMethod)
                .orElse(null);
    }

    private PaymentStatus getLatestPaymentStatus(Order o) {
        if (o.getPayments() == null || o.getPayments().isEmpty()) return null;

        return o.getPayments().stream()
                .max(Comparator.comparing(Payment::getCreatedAt))
                .map(Payment::getStatus)
                .orElse(null);
    }
}
