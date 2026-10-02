package com.farmora.dto.order;

import com.farmora.dto.auth.UserResponse;
import com.farmora.dto.product.ProductResponse;
import com.farmora.model.OrderStatus;
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
public class OrderResponse {
    private Long id;
    private String orderNumber;
    private Long bidId;
    private UserResponse buyer;
    private UserResponse farmer;
    private ProductResponse product;
    private BigDecimal quantity;
    private BigDecimal unitPrice;
    private BigDecimal totalAmount;
    private OrderStatus status;
    private String fulfillmentType;
    private String paymentStatus;
    private String paymentMethod;
    private String deliveryAddress;
    private String pickupAddress;
    private String cancellationReason;
    private String notes;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
