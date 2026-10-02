package com.farmora.dto.delivery;

import com.farmora.dto.auth.UserResponse;
import com.farmora.dto.order.OrderResponse;
import com.farmora.model.DeliveryStatus;
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
public class DeliveryResponse {
    private Long id;
    private OrderResponse order;
    private UserResponse deliveryPartner;
    private String pickupAddress;
    private String deliveryAddress;
    private DeliveryStatus status;
    private String trackingNotes;
    private String recipientPhone;
    private String specialInstructions;
    private Double currentLat;
    private Double currentLng;
    private String estimatedDeliveryTime;
    private BigDecimal deliveryFee;
    private LocalDateTime assignedAt;
    private LocalDateTime pickedUpAt;
    private LocalDateTime deliveredAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
