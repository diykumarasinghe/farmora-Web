package com.farmora.service;

import com.farmora.dto.harvest.DailyHarvestRequest;
import com.farmora.dto.harvest.DailyHarvestResponse;
import com.farmora.dto.harvest.HarvestAnalyticsResponse;
import com.farmora.exception.AccessDeniedCustomException;
import com.farmora.exception.ResourceNotFoundException;
import com.farmora.model.*;
import com.farmora.repository.DailyHarvestRepository;
import com.farmora.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DailyHarvestService {

    private final DailyHarvestRepository harvestRepository;
    private final AuthService authService;
    private final UserRepository userRepository;
    private final NotificationService notificationService;

    @Transactional
    public DailyHarvestResponse createDailyHarvest(DailyHarvestRequest request, String farmerEmail) {
        User farmer = authService.getUserByEmail(farmerEmail);
        if (farmer.getRole() != Role.FARMER && farmer.getRole() != Role.ADMIN) {
            throw new AccessDeniedCustomException("Only registered farmers can post daily harvests.");
        }

        LocalDate harvestDate = request.getHarvestDate() != null ? request.getHarvestDate() : LocalDate.now();
        LocalDate expiryDate = request.getExpiryDate() != null ? request.getExpiryDate() : harvestDate.plusDays(3);

        DailyHarvest harvest = DailyHarvest.builder()
                .farmer(farmer)
                .cropName(request.getCropName())
                .category(request.getCategory())
                .quantity(request.getQuantity())
                .initialQuantity(request.getQuantity())
                .unit(request.getUnit())
                .pricePerUnit(request.getPricePerUnit())
                .harvestDate(harvestDate)
                .expiryDate(expiryDate)
                .freshnessIndicator(request.getFreshnessIndicator() != null ? request.getFreshnessIndicator() : "Picked Today")
                .status(HarvestStatus.AVAILABLE)
                .location(request.getLocation() != null ? request.getLocation() : farmer.getAddress())
                .imageUrl(request.getImageUrl())
                .notes(request.getNotes())
                .build();

        DailyHarvest saved = harvestRepository.save(harvest);

        // Notify followers/buyers of fresh harvest alert
        notificationService.createNotification(
                farmer,
                "Daily Harvest Published",
                "Your daily harvest of " + saved.getCropName() + " (" + saved.getQuantity() + " " + saved.getUnit() + ") is now live for buyers.",
                "HARVEST_ALERT",
                saved.getId()
        );

        return mapToResponse(saved);
    }

    public DailyHarvestResponse getHarvestById(Long harvestId) {
        DailyHarvest harvest = harvestRepository.findById(harvestId)
                .orElseThrow(() -> new ResourceNotFoundException("Daily harvest not found with id: " + harvestId));
        return mapToResponse(harvest);
    }

    @Transactional
    public DailyHarvestResponse updateQuantity(Long harvestId, BigDecimal newQty, String userEmail) {
        DailyHarvest harvest = harvestRepository.findById(harvestId)
                .orElseThrow(() -> new ResourceNotFoundException("Daily harvest not found with id: " + harvestId));
        User user = authService.getUserByEmail(userEmail);

        if (!harvest.getFarmer().getId().equals(user.getId()) && user.getRole() != Role.ADMIN) {
            throw new AccessDeniedCustomException("You can only modify your own daily harvest listings.");
        }

        harvest.setQuantity(newQty);
        if (newQty.compareTo(BigDecimal.ZERO) <= 0) {
            harvest.setStatus(HarvestStatus.SOLD_OUT);
            harvest.setQuantity(BigDecimal.ZERO);
        } else if (harvest.getStatus() == HarvestStatus.SOLD_OUT) {
            harvest.setStatus(HarvestStatus.AVAILABLE);
        }

        return mapToResponse(harvestRepository.save(harvest));
    }

    @Transactional
    public DailyHarvestResponse archiveHarvest(Long harvestId, String userEmail) {
        DailyHarvest harvest = harvestRepository.findById(harvestId)
                .orElseThrow(() -> new ResourceNotFoundException("Daily harvest not found with id: " + harvestId));
        User user = authService.getUserByEmail(userEmail);

        if (!harvest.getFarmer().getId().equals(user.getId()) && user.getRole() != Role.ADMIN) {
            throw new AccessDeniedCustomException("You can only archive your own harvest listings.");
        }

        harvest.setStatus(HarvestStatus.ARCHIVED);
        return mapToResponse(harvestRepository.save(harvest));
    }

    public List<DailyHarvestResponse> getFarmerTodayHarvest(Long farmerId) {
        User farmer = userRepository.findById(farmerId)
                .orElseThrow(() -> new ResourceNotFoundException("Farmer not found with id: " + farmerId));
        return harvestRepository.findByFarmerAndHarvestDateOrderByCreatedAtDesc(farmer, LocalDate.now()).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public List<DailyHarvestResponse> getFarmerAllHarvests(Long farmerId) {
        User farmer = userRepository.findById(farmerId)
                .orElseThrow(() -> new ResourceNotFoundException("Farmer not found with id: " + farmerId));
        return harvestRepository.findByFarmerOrderByCreatedAtDesc(farmer).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public DailyHarvestResponse updateStatus(Long harvestId, HarvestStatus status, String userEmail) {
        DailyHarvest harvest = harvestRepository.findById(harvestId)
                .orElseThrow(() -> new ResourceNotFoundException("Daily harvest not found with id: " + harvestId));
        User user = authService.getUserByEmail(userEmail);

        if (!harvest.getFarmer().getId().equals(user.getId()) && user.getRole() != Role.ADMIN) {
            throw new AccessDeniedCustomException("You can only update status of your own harvest listings.");
        }

        harvest.setStatus(status);
        return mapToResponse(harvestRepository.save(harvest));
    }

    public List<DailyHarvestResponse> getAvailableNow() {
        return harvestRepository.findByStatusAndQuantityGreaterThanOrderByCreatedAtDesc(HarvestStatus.AVAILABLE, BigDecimal.ZERO).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public HarvestAnalyticsResponse getFarmerHarvestAnalytics(Long farmerId) {
        User farmer = userRepository.findById(farmerId)
                .orElseThrow(() -> new ResourceNotFoundException("Farmer not found with id: " + farmerId));

        List<DailyHarvest> harvests = harvestRepository.findByFarmerOrderByCreatedAtDesc(farmer);

        long totalPosts = harvests.size();
        long activePosts = harvests.stream().filter(h -> h.getStatus() == HarvestStatus.AVAILABLE).count();
        long soldOutPosts = harvests.stream().filter(h -> h.getStatus() == HarvestStatus.SOLD_OUT).count();

        BigDecimal totalVolume = harvests.stream()
                .map(h -> h.getInitialQuantity() != null ? h.getInitialQuantity() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal currentAvailableVolume = harvests.stream()
                .map(h -> h.getQuantity() != null ? h.getQuantity() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal soldVolume = totalVolume.subtract(currentAvailableVolume);
        if (soldVolume.compareTo(BigDecimal.ZERO) < 0) {
            soldVolume = BigDecimal.ZERO;
        }

        long todayCount = harvests.stream()
                .filter(h -> LocalDate.now().equals(h.getHarvestDate()))
                .count();

        double freshnessScore = totalPosts > 0 ? ((double) todayCount / totalPosts) * 100.0 : 100.0;
        freshnessScore = Math.round(freshnessScore * 10.0) / 10.0;

        Map<String, Long> categoryDistribution = harvests.stream()
                .collect(Collectors.groupingBy(DailyHarvest::getCategory, Collectors.counting()));

        return HarvestAnalyticsResponse.builder()
                .farmerId(farmerId)
                .totalHarvestPosts(totalPosts)
                .activeAvailablePosts(activePosts)
                .soldOutPosts(soldOutPosts)
                .totalHarvestVolume(totalVolume)
                .soldHarvestVolume(soldVolume)
                .freshnessScore(freshnessScore)
                .categoryDistribution(categoryDistribution)
                .build();
    }

    @Transactional
    public DailyHarvestResponse extendHarvestDate(Long harvestId, int extraDays, String userEmail) {
        DailyHarvest harvest = harvestRepository.findById(harvestId)
                .orElseThrow(() -> new ResourceNotFoundException("Daily harvest not found with id: " + harvestId));
        User user = authService.getUserByEmail(userEmail);

        if (!harvest.getFarmer().getId().equals(user.getId()) && user.getRole() != Role.ADMIN) {
            throw new AccessDeniedCustomException("You can only extend expiry of your own harvest listings.");
        }

        int days = extraDays > 0 ? extraDays : 2;
        LocalDate base = harvest.getExpiryDate() != null && harvest.getExpiryDate().isAfter(LocalDate.now())
                ? harvest.getExpiryDate()
                : LocalDate.now();
        harvest.setExpiryDate(base.plusDays(days));
        if (harvest.getStatus() == HarvestStatus.EXPIRED) {
            harvest.setStatus(HarvestStatus.AVAILABLE);
        }

        return mapToResponse(harvestRepository.save(harvest));
    }

    @Transactional
    public Map<String, Object> cleanupExpired() {
        LocalDate today = LocalDate.now();
        List<DailyHarvest> expired = harvestRepository.findByExpiryDateBeforeAndStatusNot(today, HarvestStatus.EXPIRED);
        for (DailyHarvest h : expired) {
            h.setStatus(HarvestStatus.EXPIRED);
        }
        harvestRepository.saveAll(expired);

        Map<String, Object> res = new HashMap<>();
        res.put("cleanedCount", expired.size());
        res.put("message", "Marked " + expired.size() + " overdue harvests as EXPIRED.");
        return res;
    }

    public DailyHarvestResponse mapToResponse(DailyHarvest h) {
        boolean isFreshToday = LocalDate.now().equals(h.getHarvestDate()) || "Picked Today".equalsIgnoreCase(h.getFreshnessIndicator());

        return DailyHarvestResponse.builder()
                .id(h.getId())
                .farmer(authService.mapToUserResponse(h.getFarmer()))
                .cropName(h.getCropName())
                .category(h.getCategory())
                .quantity(h.getQuantity())
                .initialQuantity(h.getInitialQuantity())
                .unit(h.getUnit())
                .pricePerUnit(h.getPricePerUnit())
                .harvestDate(h.getHarvestDate())
                .expiryDate(h.getExpiryDate())
                .freshnessIndicator(h.getFreshnessIndicator())
                .status(h.getStatus())
                .location(h.getLocation())
                .imageUrl(h.getImageUrl())
                .notes(h.getNotes())
                .isFreshToday(isFreshToday)
                .createdAt(h.getCreatedAt())
                .updatedAt(h.getUpdatedAt())
                .build();
    }
}
