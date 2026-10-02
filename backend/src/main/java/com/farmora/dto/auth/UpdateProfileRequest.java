package com.farmora.dto.auth;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class UpdateProfileRequest {

    @NotBlank(message = "Full name is required")
    private String fullName;

    private String phone;

    private String address;

    private String profileImageUrl;

    private com.farmora.model.BuyerType buyerType;

    private String businessName;

    private String businessRegNumber;

    private String district;

    private String preferredLanguage;
}
