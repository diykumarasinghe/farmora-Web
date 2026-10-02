package com.farmora.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "daily_harvests")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DailyHarvest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "farmer_id", nullable = false)
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler", "password"})
    private User farmer;

    @NotBlank(message = "Crop name is required")
    @Column(nullable = false, length = 100)
    private String cropName;

    @NotBlank(message = "Category is required")
    @Column(nullable = false, length = 50)
    private String category;

    @NotNull(message = "Available quantity is required")
    @DecimalMin(value = "0.00", message = "Quantity must be non-negative")
    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal quantity;

    @NotNull(message = "Initial quantity is required")
    @DecimalMin(value = "0.01", message = "Initial quantity must be greater than zero")
    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal initialQuantity;

    @NotBlank(message = "Unit is required")
    @Column(nullable = false, length = 20)
    private String unit;

    @NotNull(message = "Price per unit is required")
    @DecimalMin(value = "0.01", message = "Price must be greater than zero")
    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal pricePerUnit;

    @Column(nullable = false)
    private LocalDate harvestDate;

    @Column
    private LocalDate expiryDate;

    @Builder.Default
    @Column(length = 50)
    private String freshnessIndicator = "Picked Today";

    @Enumerated(EnumType.STRING)
    @Builder.Default
    @Column(nullable = false, length = 20)
    private HarvestStatus status = HarvestStatus.AVAILABLE;

    @Column(length = 100)
    private String location;

    @Column(length = 500)
    private String imageUrl;

    @Column(length = 500)
    private String notes;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(nullable = false)
    private LocalDateTime updatedAt;
}
