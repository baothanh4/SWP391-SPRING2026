package com.example.SWP391_SPRING2026.DTO.Response;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class ProductSearchItemDTO {
    private Long id;
    private String name;
    private String brandName;
    private String status;
    private String productImage;

    private BigDecimal minPrice;
    private BigDecimal maxPrice;

    private Long totalStock;
    private Boolean hasStock;
}
