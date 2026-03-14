package com.example.SWP391_SPRING2026.DTO.Response;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LowStockDTO {
    private Long variantId;
    private String productName;
    private Integer stockQuantity;

}
