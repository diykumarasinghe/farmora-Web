package com.farmora.dto.analytics;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductPerformanceDto {
    private Long productId;
    private String name;
    private String category;
    private BigDecimal unitsSold;
    private BigDecimal revenueGenerated;
    private BigDecimal remainingStock;
    private String unit;
    private double conversionRate;
}
