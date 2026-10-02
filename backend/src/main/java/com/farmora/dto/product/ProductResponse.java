package com.farmora.dto.product;

import com.farmora.dto.auth.UserResponse;
import com.farmora.model.ProductStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductResponse {
    private Long id;
    private String name;
    private String description;
    private String category;
    private BigDecimal price;
    private BigDecimal quantity;
    private String unit;
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
    private ProductStatus status;
    private UserResponse farmer;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
