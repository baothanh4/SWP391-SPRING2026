package com.example.SWP391_SPRING2026.DTO.Response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderItemResponseDTO {
    private Long id;
    private Boolean isCombo;
    private Long productVariantId;
    private Long comboId;
    private String productName;
    private String variantName;
    private String productImage;
    private Integer quantity;
    private Long price;
}
