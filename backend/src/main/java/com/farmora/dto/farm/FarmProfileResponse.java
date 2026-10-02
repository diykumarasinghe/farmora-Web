package com.farmora.dto.farm;

import com.farmora.dto.auth.UserResponse;
import com.farmora.model.FarmingMethod;
import com.farmora.model.VerificationStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FarmProfileResponse {

    private Long id;
    private UserResponse farmer;
    private String farmName;
    private String farmerDetails;
    private String contactPhone;
    private String contactEmail;
    private String socialLink;

    // GPS Mapping
    private Double latitude;
    private Double longitude;

    // Address & Region
    private String farmAddress;
    private String province;
    private String district;

    // Media
    private String photoUrls;
    private String videoUrl;

    // Experience & Operations
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

    // Certifications
    private String certifications;

    // Verification Badge System
    private VerificationStatus verificationStatus;
    private String verificationNotes;
    private LocalDateTime verifiedAt;

    // Rating & Feedback aggregated
    private Double averageRating;
    private Long totalReviews;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
