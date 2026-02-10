package com.example.SWP391_SPRING2026.DTO.Request;

import com.example.SWP391_SPRING2026.Enum.OrderType;
import lombok.Data;

@Data
public class CheckoutRequestDTO {
    private Long addressId;
    private OrderType orderType;
}
