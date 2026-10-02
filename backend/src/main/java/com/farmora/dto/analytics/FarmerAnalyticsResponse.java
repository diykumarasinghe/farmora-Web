package com.farmora.dto.analytics;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FarmerAnalyticsResponse {
    private BigDecimal totalRevenue;
    private long totalSalesCount;
    private BigDecimal avgOrderValue;
    private long activeListingsCount;
    private List<ProductPerformanceDto> topProducts;
    private List<PriceBenchmarkDto> priceAnalytics;
    private List<CustomerInsightDto> customerList;
    private Map<String, BigDecimal> monthlyRevenue;
}
