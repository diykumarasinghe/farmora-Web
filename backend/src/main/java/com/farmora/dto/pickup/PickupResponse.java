package com.farmora.dto.pickup;

import com.farmora.dto.auth.UserResponse;
import com.farmora.dto.order.OrderResponse;
import com.farmora.model.PickupStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PickupResponse {
    private Long id;
    private OrderResponse order;
    private UserResponse buyer;
    private UserResponse farmer;
    private LocalDate pickupDate;
    private String timeSlot;
    private String vehicleNumber;
    private String contactPhone;
    private String passcode;
    private PickupStatus status;
    private String pickupLocation;
    private String instructions;
    private String cancellationReason;
    private String rescheduledReason;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
