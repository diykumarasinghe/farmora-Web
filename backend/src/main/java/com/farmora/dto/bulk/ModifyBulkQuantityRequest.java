package com.farmora.dto.bulk;

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
public class ModifyBulkQuantityRequest {
    @NotNull(message = "Quantity is required")
    @DecimalMin(value = "1.00", message = "Quantity must be at least 1")
    private BigDecimal quantity;

    private BigDecimal targetPricePerUnit;
    private String specialRequirements;
}
