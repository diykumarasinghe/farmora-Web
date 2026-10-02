package com.farmora.dto.analytics;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CustomerInsightDto {
    private Long buyerId;
    private String fullName;
    private String email;
    private String location;
    private long totalOrdersPlaced;
    private BigDecimal totalAmountSpent;
    private LocalDateTime lastOrderDate;
    private boolean isRepeatBuyer;
}
