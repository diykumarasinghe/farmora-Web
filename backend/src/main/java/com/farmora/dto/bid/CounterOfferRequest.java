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
public class CounterOfferRequest {
    @NotNull(message = "Counter offer amount is required")
    @DecimalMin(value = "0.01", message = "Counter offer amount must be greater than 0")
    private BigDecimal counterAmount;

    private String counterNote;
}
