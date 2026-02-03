package com.example.SWP391_SPRING2026.DTO.Response;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class CartResponseDTO {
    private List<CartItemResponseDTO> items;

    private BigDecimal subTotal;
    private BigDecimal discountAmount;
    private BigDecimal finalTotal;

    private String couponCode;
    private String orderNote;
    private Boolean requestInvoice;

    // hỗ trợ Empty State cho FE
    private Integer totalItems;
    private Boolean empty;
}
