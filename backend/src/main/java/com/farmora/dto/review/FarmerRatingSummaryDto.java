package com.farmora.dto.review;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FarmerRatingSummaryDto {
    private Long farmerId;
    private Double averageRating;
    private Long totalReviews;
    private Map<Integer, Long> ratingCounts; // { 5: count, 4: count, ... }
}
