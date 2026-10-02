package com.farmora.dto.bid;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BidRequest {

    @NotNull(message = "Product ID is required")
    private Long productId;

    @NotNull(message = "Bid unit offer is required")
    @DecimalMin(value = "0.01", message = "Bid unit offer must be greater than 0")
    private BigDecimal amount;

    @NotNull(message = "Bid quantity is required")
    @DecimalMin(value = "0.01", message = "Bid quantity must be greater than 0")
    private BigDecimal quantity;

    private String note;
}
