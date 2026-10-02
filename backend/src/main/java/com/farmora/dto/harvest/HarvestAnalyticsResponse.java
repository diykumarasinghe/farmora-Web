package com.farmora.dto.harvest;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HarvestAnalyticsResponse {
    private Long farmerId;
    private long totalHarvestPosts;
    private long activeAvailablePosts;
    private long soldOutPosts;
    private BigDecimal totalHarvestVolume; // in kg/units
    private BigDecimal soldHarvestVolume;
    private double freshnessScore; // percentage of harvested today
    private Map<String, Long> categoryDistribution;
}
