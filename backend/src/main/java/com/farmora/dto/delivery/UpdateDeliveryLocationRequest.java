package com.farmora.dto.delivery;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateDeliveryLocationRequest {
    private Double currentLat;
    private Double currentLng;
    private String trackingNotes;
}
