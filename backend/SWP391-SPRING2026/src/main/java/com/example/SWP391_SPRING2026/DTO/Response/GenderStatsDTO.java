package com.example.SWP391_SPRING2026.DTO.Response;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class GenderStatsDTO {
    private Integer gender;
    private Long count;
    private Double percentage;
}
