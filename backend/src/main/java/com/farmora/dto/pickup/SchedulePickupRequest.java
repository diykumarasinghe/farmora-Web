package com.farmora.dto.pickup;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SchedulePickupRequest {
    @NotNull(message = "Order ID is required")
    private Long orderId;

    @NotNull(message = "Pickup date is required")
    private LocalDate pickupDate;

    @NotNull(message = "Time slot is required")
    private String timeSlot;

    private String vehicleNumber;
    private String contactPhone;
    private String instructions;
}
