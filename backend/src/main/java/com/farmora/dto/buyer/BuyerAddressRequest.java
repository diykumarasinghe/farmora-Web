package com.farmora.dto.buyer;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BuyerAddressRequest {

    @NotBlank(message = "Address label is required (e.g. Home, Kitchen, Central Warehouse)")
    private String label;

    private String recipientName;
    private String phone;

    @NotBlank(message = "Street address is required")
    private String streetAddress;

    private String city;
    private String district;
    private String province;
    private String postalCode;

    private Boolean isDefault;
}
