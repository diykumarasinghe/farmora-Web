package com.farmora.dto.analytics;

import com.farmora.dto.product.ProductResponse;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BuyerAnalyticsResponse {
    private BigDecimal totalPurchasedAmount;
    private long totalOrdersCount;
    private long activeBidsCount;
    private long favoriteFarmsCount;
    private long savedProductsCount;
    private List<ProductResponse> savedProducts;
}
