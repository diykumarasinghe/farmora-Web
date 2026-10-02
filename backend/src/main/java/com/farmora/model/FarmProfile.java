package com.farmora.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "farm_profiles")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FarmProfile {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "farmer_id", nullable = false, unique = true)
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler", "password"})
    private User farmer;

    @Column(nullable = false, length = 150)
    private String farmName;

    @Column(columnDefinition = "TEXT")
    private String farmerDetails;

    @Column(length = 30)
    private String contactPhone;

    @Column(length = 150)
    private String contactEmail;

    @Column(length = 255)
    private String socialLink;

    // GPS Mapping
    private Double latitude;
    private Double longitude;

    // Farm Address & Region
    @Column(length = 255)
    private String farmAddress;

    @Column(length = 100)
    private String province;

    @Column(length = 100)
    private String district;

    // Media
    @Column(columnDefinition = "TEXT")
    private String photoUrls;

    @Column(length = 500)
    private String videoUrl;

    // Experience & Operations
    private Integer farmingExperience; // in years

    private Double farmSize;

    @Column(length = 20)
    @Builder.Default
    private String farmSizeUnit = "Acres";

    @Enumerated(EnumType.STRING)
    @Column(length = 30)
    @Builder.Default
    private FarmingMethod farmingMethod = FarmingMethod.ORGANIC;

    // Additional Profile Attributes
    @Column(length = 500)
    private String coverImageUrl;

    @Column(length = 255)
    private String tagline;

    @Column(length = 255)
    private String quote;

    @Column(length = 255)
    private String primaryCrops;

    @Column(length = 100)
    private String farmType;

    private Integer establishedYear;

    @Column(length = 255)
    private String specialization;

    @Column(columnDefinition = "TEXT")
    private String skills;

    @Column(columnDefinition = "TEXT")
    private String farmingMethodsList;

    @Column(length = 50)
    private String nicNumber;

    @Column(length = 50)
    private String dateOfBirth;

    @Builder.Default
    @Column(nullable = false)
    private Boolean isArchived = false;

    @Column(columnDefinition = "TEXT")
    private String archivedSections;

    // Certifications (JSON string or text list)
    @Column(columnDefinition = "TEXT")
    private String certifications;

    // Verification Badge System
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    @Builder.Default
    private VerificationStatus verificationStatus = VerificationStatus.UNVERIFIED;

    @Column(length = 150)
    private String languagesSpoken; // e.g. Sinhala, Tamil, English

    @Column(length = 100)
    private String farmCategory; // e.g. Vegetables, Fruits, Rice, Spices, Dairy

    @Builder.Default
    @Column(nullable = false)
    private Boolean pickupAvailable = true;

    @Builder.Default
    @Column(nullable = false)
    private Boolean homeDeliveryAvailable = true;

    private Double deliveryRadius; // km

    private Double deliveryFee; // Rs.

    @Column(length = 150)
    private String bankAccount;

    @Column(length = 100)
    private String mobileWallet;

    @Column(length = 500)
    private String nicDocumentUrl;

    @Column(columnDefinition = "TEXT")
    private String verificationNotes;

    private LocalDateTime verifiedAt;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(nullable = false)
    private LocalDateTime updatedAt;
}
