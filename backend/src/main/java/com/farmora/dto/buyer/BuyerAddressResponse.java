package com.farmora.dto.buyer;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BuyerAddressResponse {

    private Long id;
    private Long buyerId;
    private String label;
    private String recipientName;
    private String phone;
    private String streetAddress;
    private String city;
    private String district;
    private String province;
    private String postalCode;
    private Boolean isDefault;
    private LocalDateTime createdAt;
}
