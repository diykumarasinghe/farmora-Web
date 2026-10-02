package com.farmora.dto.bid;

import com.farmora.dto.auth.UserResponse;
import com.farmora.dto.product.ProductResponse;
import com.farmora.model.BidStatus;
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
public class BidResponse {
    private Long id;
    private ProductResponse product;
    private UserResponse buyer;
    private BigDecimal amount;
    private BigDecimal quantity;
    private BigDecimal totalOffer;
    private BidStatus status;
    private String note;
    private BigDecimal counterAmount;
    private String counterNote;
    private String counterBy;
    private LocalDateTime expiresAt;
    private Boolean archived;
    private Boolean isExpired;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
