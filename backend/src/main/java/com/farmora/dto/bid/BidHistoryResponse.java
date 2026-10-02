package com.farmora.dto.bid;

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
public class BidHistoryResponse {
    private Long id;
    private Long bidId;
    private String actorRole;
    private String actorName;
    private BigDecimal offeredAmount;
    private BigDecimal offeredQuantity;
    private String note;
    private String action;
    private LocalDateTime timestamp;
}
