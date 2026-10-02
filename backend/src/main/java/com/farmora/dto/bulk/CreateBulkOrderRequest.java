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
public class CreateBulkOrderRequest {
    @NotNull(message = "Product ID is required")
    private Long productId;

    @NotNull(message = "Quantity is required")
    @DecimalMin(value = "1.00", message = "Quantity must be at least 1")
    private BigDecimal quantity;

    @NotNull(message = "Target unit price is required")
    @DecimalMin(value = "0.01", message = "Target unit price must be greater than 0")
    private BigDecimal targetPricePerUnit;

    private String packagingType;
    private String deliveryFrequency;
    private String requiredDate;
    private String specialRequirements;
    private String fulfillmentType; // "DELIVERY" or "PICKUP"
    private String destinationAddress;
}
