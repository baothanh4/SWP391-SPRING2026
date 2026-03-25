package com.example.SWP391_SPRING2026.DTO.Response;

import com.example.SWP391_SPRING2026.Entity.Shipment;
import com.example.SWP391_SPRING2026.Enum.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderResponseDTO {

    private Long id;
    private String orderCode;
    private OrderType orderType;
    private OrderStatus orderStatus;
    private Long totalAmount;
    private Long deposit;
    private Long remainingAmount;
    private AddressResponseDTO address;
    private LocalDateTime createdAt;
    private PaymentMethod paymentMethod;
    private PaymentStatus paymentStatus;
    private String ghnOrderCode;
    private ShipmentStatus shipmentStatus;
    private ApprovalStatus approvalStatus;
    private LocalDateTime supportApprovedAt;
    private LocalDateTime operationConfirmedAt;
    private LocalDateTime deliveredAt;
    private List<OrderItemResponseDTO> items;

    public OrderResponseDTO(Long id, String orderCode, OrderType orderType, OrderStatus orderStatus, Long totalAmount, Long deposit, Long remainingAmount, AddressResponseDTO addressResponseDTO, LocalDateTime createdAt, PaymentMethod latestPaymentMethod, PaymentStatus latestPaymentStatus, String s, ShipmentStatus shipmentStatus, ApprovalStatus approvalStatus, LocalDateTime supportApprovedAt, LocalDateTime operationConfirmedAt, LocalDateTime localDateTime) {
    }
}
