package com.example.SWP391_SPRING2026.Repository.Projection;

import com.example.SWP391_SPRING2026.Enum.ProductStatus;

import java.math.BigDecimal;

public interface ProductSearchProjection {
    Long getId();
    String getName();
    String getBrandName();
    ProductStatus getStatus();
    String getProductImage();

    BigDecimal getMinPrice();
    BigDecimal getMaxPrice();

    Long getTotalStock();
}
