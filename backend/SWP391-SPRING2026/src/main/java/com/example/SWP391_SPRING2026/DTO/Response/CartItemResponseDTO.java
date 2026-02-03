package com.example.SWP391_SPRING2026.DTO.Response;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class CartItemResponseDTO {
    private Long productId;
    private String productName;
    private String productImage;

    private BigDecimal unitPrice;
    private Integer quantity;
    private BigDecimal totalPrice;
}
