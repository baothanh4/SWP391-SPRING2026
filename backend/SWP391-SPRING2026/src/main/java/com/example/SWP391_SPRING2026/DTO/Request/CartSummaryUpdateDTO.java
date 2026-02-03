package com.example.SWP391_SPRING2026.DTO.Request;

import lombok.Data;

@Data
public class CartSummaryUpdateDTO {
    private String couponCode;
    private String orderNote;
    private Boolean requestInvoice;
}
