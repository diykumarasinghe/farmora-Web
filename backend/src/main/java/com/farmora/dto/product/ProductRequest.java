package com.farmora.dto.product;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductRequest {

    @NotBlank(message = "Product name is required")
    @Size(max = 150)
    private String name;

    private String description;

    @NotBlank(message = "Category is required")
    @Size(max = 50)
    private String category;

    @NotNull(message = "Price is required")
    @DecimalMin(value = "0.01", message = "Price must be greater than 0")
    private BigDecimal price;

    @NotNull(message = "Quantity is required")
    @DecimalMin(value = "0.01", message = "Quantity must be greater than 0")
    private BigDecimal quantity;

    @NotBlank(message = "Unit is required")
    @Size(max = 20)
    private String unit;

    @Size(max = 100)
    private String location;

    private LocalDate harvestDate;

    private String imageUrl;
    private String videoUrl;
    private LocalDate expiryDate;
    private String farmingMethod;
    private String qualityGrade;
    private String packagingInfo;
    private BigDecimal minOrderQuantity;
    private BigDecimal wholesalePrice;
    private Boolean bulkOrderAvailable;
}
