package com.farmora.dto.harvest;

import com.farmora.dto.auth.UserResponse;
import com.farmora.model.HarvestStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DailyHarvestResponse {
    private Long id;
    private UserResponse farmer;
    private String cropName;
    private String category;
    private BigDecimal quantity;
    private BigDecimal initialQuantity;
    private String unit;
    private BigDecimal pricePerUnit;
    private LocalDate harvestDate;
    private LocalDate expiryDate;
    private String freshnessIndicator;
    private HarvestStatus status;
    private String location;
    private String imageUrl;
    private String notes;
    private Boolean isFreshToday;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
