package com.farmora.service;

import com.farmora.dto.analytics.*;
import com.farmora.dto.product.ProductResponse;
import com.farmora.exception.ResourceNotFoundException;
import com.farmora.model.*;
import com.farmora.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AnalyticsService {

    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;
    private final BidRepository bidRepository;
    private final FavouriteFarmRepository favouriteFarmRepository;
    private final SavedProductRepository savedProductRepository;
    private final AuthService authService;
    private final ProductService productService;

    public FarmerAnalyticsResponse getFarmerAnalytics(String farmerEmail) {
        User farmer = authService.getUserByEmail(farmerEmail);
        List<Order> orders = orderRepository.findByFarmerOrderByCreatedAtDesc(farmer);
        List<Product> products = productRepository.findByFarmerOrderByCreatedAtDesc(farmer);

        BigDecimal totalRevenue = orders.stream()
                .map(o -> o.getTotalAmount() != null ? o.getTotalAmount() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        long totalSalesCount = orders.size();
        BigDecimal avgOrderValue = totalSalesCount > 0
                ? totalRevenue.divide(BigDecimal.valueOf(totalSalesCount), 2, RoundingMode.HALF_UP)
                : BigDecimal.ZERO;

        long activeListings = products.stream()
                .filter(p -> p.getStatus() == ProductStatus.AVAILABLE)
                .count();

        // Product performance
        Map<Long, List<Order>> ordersByProduct = orders.stream()
                .filter(o -> o.getProduct() != null)
                .collect(Collectors.groupingBy(o -> o.getProduct().getId()));

        List<ProductPerformanceDto> performanceList = new ArrayList<>();
        for (Product p : products) {
            List<Order> pOrders = ordersByProduct.getOrDefault(p.getId(), Collections.emptyList());
            BigDecimal soldQty = pOrders.stream()
                    .map(o -> o.getQuantity() != null ? o.getQuantity() : BigDecimal.ZERO)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            BigDecimal revenue = pOrders.stream()
                    .map(o -> o.getTotalAmount() != null ? o.getTotalAmount() : BigDecimal.ZERO)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            double conversion = (p.getQuantity().compareTo(BigDecimal.ZERO) > 0)
                    ? Math.min(100.0, Math.round((soldQty.doubleValue() / (soldQty.doubleValue() + p.getQuantity().doubleValue())) * 1000.0) / 10.0)
                    : 100.0;

            performanceList.add(ProductPerformanceDto.builder()
                    .productId(p.getId())
                    .name(p.getName())
                    .category(p.getCategory())
                    .unitsSold(soldQty)
                    .revenueGenerated(revenue)
                    .remainingStock(p.getQuantity())
                    .unit(p.getUnit())
                    .conversionRate(conversion)
                    .build());
        }

        // Sort top revenue
        performanceList.sort((a, b) -> b.getRevenueGenerated().compareTo(a.getRevenueGenerated()));

        // Customer Insights (analyze buyers)
        Map<Long, List<Order>> ordersByBuyer = orders.stream()
                .filter(o -> o.getBuyer() != null)
                .collect(Collectors.groupingBy(o -> o.getBuyer().getId()));

        List<CustomerInsightDto> customerInsights = new ArrayList<>();
        for (Map.Entry<Long, List<Order>> entry : ordersByBuyer.entrySet()) {
            List<Order> bOrders = entry.getValue();
            User buyer = bOrders.get(0).getBuyer();

            BigDecimal spent = bOrders.stream()
                    .map(o -> o.getTotalAmount() != null ? o.getTotalAmount() : BigDecimal.ZERO)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            customerInsights.add(CustomerInsightDto.builder()
                    .buyerId(buyer.getId())
                    .fullName(buyer.getFullName())
                    .email(buyer.getEmail())
                    .location(buyer.getAddress() != null ? buyer.getAddress() : "Western Province")
                    .totalOrdersPlaced(bOrders.size())
                    .totalAmountSpent(spent)
                    .lastOrderDate(bOrders.get(0).getCreatedAt())
                    .isRepeatBuyer(bOrders.size() > 1)
                    .build());
        }
        customerInsights.sort((a, b) -> Long.compare(b.getTotalOrdersPlaced(), a.getTotalOrdersPlaced()));

        // Price Benchmarks
        List<PriceBenchmarkDto> priceBenchmarks = new ArrayList<>();
        for (Product p : products) {
            BigDecimal marketAvg = p.getPrice().multiply(BigDecimal.valueOf(1.05)).setScale(2, RoundingMode.HALF_UP);
            String trend = p.getPrice().compareTo(marketAvg) > 0 ? "PREMIUM" : (p.getPrice().compareTo(marketAvg) < 0 ? "COMPETITIVE" : "PARITY");

            priceBenchmarks.add(PriceBenchmarkDto.builder()
                    .category(p.getCategory())
                    .cropName(p.getName())
                    .myAvgPrice(p.getPrice())
                    .marketAvgPrice(marketAvg)
                    .unit(p.getUnit())
                    .trend(trend)
                    .build());
        }

        // Monthly revenue map
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("MMM yyyy");
        Map<String, BigDecimal> monthlyRevenue = new LinkedHashMap<>();
        for (Order o : orders) {
            if (o.getCreatedAt() != null) {
                String month = o.getCreatedAt().format(fmt);
                monthlyRevenue.put(month, monthlyRevenue.getOrDefault(month, BigDecimal.ZERO).add(o.getTotalAmount() != null ? o.getTotalAmount() : BigDecimal.ZERO));
            }
        }

        return FarmerAnalyticsResponse.builder()
                .totalRevenue(totalRevenue)
                .totalSalesCount(totalSalesCount)
                .avgOrderValue(avgOrderValue)
                .activeListingsCount(activeListings)
                .topProducts(performanceList)
                .customerList(customerInsights)
                .priceAnalytics(priceBenchmarks)
                .monthlyRevenue(monthlyRevenue)
                .build();
    }

    public BuyerAnalyticsResponse getBuyerAnalytics(String buyerEmail) {
        User buyer = authService.getUserByEmail(buyerEmail);
        List<Order> orders = orderRepository.findByBuyerOrderByCreatedAtDesc(buyer);
        List<Bid> bids = bidRepository.findByBuyerOrderByCreatedAtDesc(buyer);
        List<FavouriteFarm> favFarms = favouriteFarmRepository.findByBuyerOrderByCreatedAtDesc(buyer);
        List<SavedProduct> savedProds = savedProductRepository.findByBuyerOrderByCreatedAtDesc(buyer);

        BigDecimal totalPurchased = orders.stream()
                .map(o -> o.getTotalAmount() != null ? o.getTotalAmount() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        long activeBids = bids.stream()
                .filter(b -> b.getStatus() == BidStatus.PENDING || b.getStatus() == BidStatus.COUNTERED)
                .count();

        List<ProductResponse> savedList = savedProds.stream()
                .map(sp -> productService.mapToResponse(sp.getProduct()))
                .collect(Collectors.toList());

        return BuyerAnalyticsResponse.builder()
                .totalPurchasedAmount(totalPurchased)
                .totalOrdersCount(orders.size())
                .activeBidsCount(activeBids)
                .favoriteFarmsCount(favFarms.size())
                .savedProductsCount(savedProds.size())
                .savedProducts(savedList)
                .build();
    }

    @Transactional
    public void saveProduct(Long productId, String buyerEmail) {
        User buyer = authService.getUserByEmail(buyerEmail);
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + productId));

        if (!savedProductRepository.existsByBuyerAndProduct(buyer, product)) {
            savedProductRepository.save(SavedProduct.builder()
                    .buyer(buyer)
                    .product(product)
                    .build());
        }
    }

    @Transactional
    public void removeSavedProduct(Long productId, String buyerEmail) {
        User buyer = authService.getUserByEmail(buyerEmail);
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + productId));
        savedProductRepository.deleteByBuyerAndProduct(buyer, product);
    }

    public List<ProductResponse> getSavedProducts(String buyerEmail) {
        User buyer = authService.getUserByEmail(buyerEmail);
        return savedProductRepository.findByBuyerOrderByCreatedAtDesc(buyer).stream()
                .map(sp -> productService.mapToResponse(sp.getProduct()))
                .collect(Collectors.toList());
    }
}
