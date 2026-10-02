package com.farmora.dto.bulk;

import com.farmora.dto.auth.UserResponse;
import com.farmora.dto.product.ProductResponse;
import com.farmora.model.BulkOrderStatus;
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
public class BulkOrderResponse {
    private Long id;
    private String bulkOrderNumber;
    private ProductResponse product;
    private UserResponse buyer;
    private UserResponse farmer;
    private BigDecimal quantity;
    private BigDecimal targetPricePerUnit;
    private BigDecimal totalEstimatedAmount;
    private String packagingType;
    private String deliveryFrequency;
    private String requiredDate;
    private String specialRequirements;
    private String fulfillmentType;
    private String destinationAddress;
    private BulkOrderStatus status;
    private String rejectionOrCancellationReason;
    private String farmerNotes;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
