package com.farmora.dto.delivery;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RedirectDeliveryRequest {
    @NotBlank(message = "New delivery address is required")
    private String newDeliveryAddress;

    private String newRecipientPhone;
    private String instructions;
}
