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
public class PriceBenchmarkDto {
    private String category;
    private String cropName;
    private BigDecimal myAvgPrice;
    private BigDecimal marketAvgPrice;
    private String unit;
    private String trend; // "HIGHER", "COMPETITIVE", "LOWER"
}
