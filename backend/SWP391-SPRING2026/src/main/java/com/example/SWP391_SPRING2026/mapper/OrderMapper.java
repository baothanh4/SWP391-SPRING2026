package com.example.SWP391_SPRING2026.mapper;

import com.example.SWP391_SPRING2026.DTO.Response.AddressResponseDTO;
import com.example.SWP391_SPRING2026.DTO.Response.OrderItemResponseDTO;
import com.example.SWP391_SPRING2026.DTO.Response.OrderResponseDTO;
import com.example.SWP391_SPRING2026.Entity.Address;
import com.example.SWP391_SPRING2026.Entity.Order;
import com.example.SWP391_SPRING2026.Entity.OrderItems;
import com.example.SWP391_SPRING2026.Entity.Payment;
import com.example.SWP391_SPRING2026.Entity.ProductCombo;
import com.example.SWP391_SPRING2026.Entity.ProductVariant;
import com.example.SWP391_SPRING2026.Entity.VariantAttribute;
import com.example.SWP391_SPRING2026.Enum.PaymentMethod;
import com.example.SWP391_SPRING2026.Enum.PaymentStatus;
import com.example.SWP391_SPRING2026.Enum.ShipmentStatus;

import java.util.Comparator;
import java.util.List;

public class OrderMapper {

    public static OrderResponseDTO toResponse(Order order) {

        Address a = order.getAddress();

        AddressResponseDTO addressDTO = null;

        if (a != null) {
            addressDTO = new AddressResponseDTO(
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

        // ================= PAYMENT =================
        PaymentMethod paymentMethod = null;
        PaymentStatus paymentStatus = null;

        if (order.getPayments() != null && !order.getPayments().isEmpty()) {

            Payment latestPayment = order.getPayments()
                    .stream()
                    .max(Comparator.comparing(Payment::getCreatedAt))
                    .orElse(null);

            if (latestPayment != null) {
                paymentMethod = latestPayment.getMethod();
                paymentStatus = latestPayment.getStatus();
            }
        }

        // ================= SHIPMENT =================
        String ghnCode = null;
        ShipmentStatus status = null;
        if (order.getShipment() != null) {
            ghnCode = order.getShipment().getGhnOrderCode();
            status = order.getShipment().getStatus();
        }

        return new OrderResponseDTO(
                order.getId(),
                order.getOrderCode(),
                order.getOrderType(),
                order.getOrderStatus(),
                order.getTotalAmount(),
                order.getDeposit(),
                order.getRemainingAmount(),
                addressDTO,
                order.getCreatedAt(),
                paymentMethod,
                paymentStatus,
                ghnCode,
                status,
                order.getApprovalStatus(),
                order.getSupportApprovedAt(),
                order.getOperationConfirmedAt(),
                order.getShipment().getDeliveredAt(),
                mapItems(order.getOrderItems())
        );
    }

    public static List<OrderItemResponseDTO> mapItems(List<OrderItems> orderItems) {
        if (orderItems == null) return List.of();

        return orderItems.stream()
                .map(OrderMapper::mapItem)
                .toList();
    }

    private static OrderItemResponseDTO mapItem(OrderItems item) {
        ProductVariant productVariant = item.getProductVariant();
        ProductCombo productCombo = item.getProductCombo();
        boolean isCombo = Boolean.TRUE.equals(item.getIsCombo());

        String productName = null;
        String variantName = null;
        String productImage = null;

        if (isCombo && productCombo != null) {
            productName = productCombo.getName();
            variantName = "Combo";
            productImage = productCombo.getImageUrl();
        } else if (productVariant != null) {
            if (productVariant.getProduct() != null) {
                productName = productVariant.getProduct().getName();
                productImage = productVariant.getProduct().getProductImage();
            }
            variantName = buildVariantName(productVariant);
        }

        return new OrderItemResponseDTO(
                item.getId(),
                isCombo,
                productVariant == null ? null : productVariant.getId(),
                productCombo == null ? null : productCombo.getId(),
                productName,
                variantName,
                productImage,
                item.getQuantity(),
                item.getPrice()
        );
    }

    private static String buildVariantName(ProductVariant productVariant) {
        if (productVariant == null) return null;
        if (productVariant.getAttributes() != null && !productVariant.getAttributes().isEmpty()) {
            return productVariant.getAttributes().stream()
                    .map(OrderMapper::formatAttribute)
                    .filter(value -> value != null && !value.isBlank())
                    .reduce((left, right) -> left + " | " + right)
                    .orElse(productVariant.getSku());
        }
        return productVariant.getSku();
    }

    private static String formatAttribute(VariantAttribute attribute) {
        if (attribute == null) return null;
        String name = attribute.getAttributeName();
        String value = attribute.getAttributeValue();
        if (name == null && value == null) return null;
        if (name == null) return value;
        if (value == null) return name;
        return name + ": " + value;
    }
}
