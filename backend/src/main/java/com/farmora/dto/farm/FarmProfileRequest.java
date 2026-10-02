package com.farmora.dto.farm;

import com.farmora.model.FarmingMethod;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FarmProfileRequest {

    @NotBlank(message = "Farm name is required")
    private String farmName;

    private String farmerDetails;
    private String contactPhone;
    private String contactEmail;
    private String socialLink;

    // GPS Mapping
    private Double latitude;
    private Double longitude;

    // Location
    private String farmAddress;
    private String province;
    private String district;

    // Media
    private String photoUrls; // comma separated or JSON string
    private String videoUrl;

    // Operations
    private Integer farmingExperience;
    private Double farmSize;
    private String farmSizeUnit;
    private FarmingMethod farmingMethod;

    // Additional Profile Attributes
    private String coverImageUrl;
    private String tagline;
    private String quote;
    private String primaryCrops;
    private String farmType;
    private Integer establishedYear;
    private String specialization;
    private String skills;
    private String farmingMethodsList;
    private String nicNumber;
    private String dateOfBirth;
    private Boolean isArchived;
    private String archivedSections;

    // Certifications & Verification
    private String certifications;
    private String nicDocumentUrl;

    // Personal & Language
    private String languagesSpoken;

    // Farm Category
    private String farmCategory;

    // Delivery Options
    private Boolean pickupAvailable;
    private Boolean homeDeliveryAvailable;
    private Double deliveryRadius;
    private Double deliveryFee;

    // Payment Details
    private String bankAccount;
    private String mobileWallet;
}
