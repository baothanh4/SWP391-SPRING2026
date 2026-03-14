package com.example.SWP391_SPRING2026.DTO.Response;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class BestSellerDTO {
    private String productName;
    private Long totalSold;
}
