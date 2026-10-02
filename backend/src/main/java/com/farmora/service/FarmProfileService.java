package com.farmora.service;

import com.farmora.dto.farm.FarmProfileRequest;
import com.farmora.dto.farm.FarmProfileResponse;
import com.farmora.exception.AccessDeniedCustomException;
import com.farmora.exception.BusinessException;
import com.farmora.exception.ResourceNotFoundException;
import com.farmora.model.*;
import com.farmora.repository.FarmProfileRepository;
import com.farmora.repository.ReviewRepository;
import com.farmora.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class FarmProfileService {

    private final FarmProfileRepository farmProfileRepository;
    private final UserRepository userRepository;
    private final AuthService authService;
    private final ReviewRepository reviewRepository;
    private final NotificationService notificationService;

    public FarmProfileResponse getMyFarmProfile(String farmerEmail) {
        User farmer = authService.getUserByEmail(farmerEmail);
        if (farmer.getRole() != Role.FARMER) {
            throw new AccessDeniedCustomException("Only farmers can access a farm profile.");
        }
        FarmProfile profile = farmProfileRepository.findByFarmer(farmer)
                .orElse(null);

        if (profile == null) {
            // Return empty draft profile tied to farmer
            return FarmProfileResponse.builder()
                    .farmer(authService.mapToUserResponse(farmer))
                    .farmName(farmer.getFullName() != null ? farmer.getFullName() + "'s Farm" : "My Farm")
                    .farmAddress(farmer.getAddress())
                    .contactPhone(farmer.getPhone())
                    .contactEmail(farmer.getEmail())
                    .verificationStatus(VerificationStatus.UNVERIFIED)
                    .farmingMethod(FarmingMethod.ORGANIC)
                    .farmSizeUnit("Acres")
                    .farmingExperience(5)
                    .farmSize(12.0)
                    .farmType("Organic")
                    .primaryCrops("Tomatoes, Carrots, Lettuce")
                    .tagline("Growing healthy food for a better tomorrow")
                    .quote("Healthy food, healthy people, healthier planet.")
                    .specialization("Vegetable Cultivation")
                    .farmingMethodsList("Organic, Mulching, Drip Irrigation")
                    .skills("Crop Management, Soil Conservation, Pest Control")
                    .isArchived(false)
                    .averageRating(4.8)
                    .totalReviews(0L)
                    .build();
        }

        return mapToResponse(profile);
    }

    @Transactional
    public FarmProfileResponse createOrUpdateFarmProfile(FarmProfileRequest request, String farmerEmail) {
        User farmer = authService.getUserByEmail(farmerEmail);
        if (farmer.getRole() != Role.FARMER) {
            throw new AccessDeniedCustomException("Only farmers can create or update a farm profile.");
        }

        FarmProfile profile = farmProfileRepository.findByFarmer(farmer)
                .orElse(FarmProfile.builder()
                        .farmer(farmer)
                        .verificationStatus(VerificationStatus.UNVERIFIED)
                        .isArchived(false)
                        .build());

        profile.setFarmName(request.getFarmName().trim());
        profile.setFarmerDetails(request.getFarmerDetails() != null ? request.getFarmerDetails().trim() : null);
        profile.setContactPhone(request.getContactPhone() != null ? request.getContactPhone().trim() : null);
        profile.setContactEmail(request.getContactEmail() != null ? request.getContactEmail().trim() : null);
        profile.setSocialLink(request.getSocialLink() != null ? request.getSocialLink().trim() : null);

        profile.setLatitude(request.getLatitude());
        profile.setLongitude(request.getLongitude());

        profile.setFarmAddress(request.getFarmAddress() != null ? request.getFarmAddress().trim() : null);
        profile.setProvince(request.getProvince() != null ? request.getProvince().trim() : null);
        profile.setDistrict(request.getDistrict() != null ? request.getDistrict().trim() : null);

        profile.setPhotoUrls(request.getPhotoUrls() != null ? request.getPhotoUrls().trim() : null);
        profile.setVideoUrl(request.getVideoUrl() != null ? request.getVideoUrl().trim() : null);

        profile.setFarmingExperience(request.getFarmingExperience());
        profile.setFarmSize(request.getFarmSize());
        if (request.getFarmSizeUnit() != null) {
            profile.setFarmSizeUnit(request.getFarmSizeUnit().trim());
        }
        if (request.getFarmingMethod() != null) {
            profile.setFarmingMethod(request.getFarmingMethod());
        }
        profile.setCertifications(request.getCertifications());

        // Additional profile fields
        profile.setCoverImageUrl(request.getCoverImageUrl() != null ? request.getCoverImageUrl().trim() : null);
        profile.setTagline(request.getTagline() != null ? request.getTagline().trim() : null);
        profile.setQuote(request.getQuote() != null ? request.getQuote().trim() : null);
        profile.setPrimaryCrops(request.getPrimaryCrops() != null ? request.getPrimaryCrops().trim() : null);
        profile.setFarmType(request.getFarmType() != null ? request.getFarmType().trim() : null);
        profile.setEstablishedYear(request.getEstablishedYear());
        profile.setSpecialization(request.getSpecialization() != null ? request.getSpecialization().trim() : null);
        profile.setSkills(request.getSkills() != null ? request.getSkills().trim() : null);
        profile.setFarmingMethodsList(request.getFarmingMethodsList() != null ? request.getFarmingMethodsList().trim() : null);
        profile.setNicNumber(request.getNicNumber() != null ? request.getNicNumber().trim() : null);
        profile.setDateOfBirth(request.getDateOfBirth() != null ? request.getDateOfBirth().trim() : null);
        if (request.getIsArchived() != null) {
            profile.setIsArchived(request.getIsArchived());
        }
        if (request.getArchivedSections() != null) {
            profile.setArchivedSections(request.getArchivedSections());
        }

        // Keep farmer user address synchronized if provided
        if (request.getFarmAddress() != null && !request.getFarmAddress().isBlank()) {
            farmer.setAddress(request.getFarmAddress().trim());
            userRepository.save(farmer);
        }

        FarmProfile saved = farmProfileRepository.save(profile);
        return mapToResponse(saved);
    }

    @Transactional
    public FarmProfileResponse toggleArchiveProfile(String farmerEmail) {
        User farmer = authService.getUserByEmail(farmerEmail);
        FarmProfile profile = farmProfileRepository.findByFarmer(farmer)
                .orElseThrow(() -> new BusinessException("Farm profile not found. Please create one first."));

        boolean current = Boolean.TRUE.equals(profile.getIsArchived());
        profile.setIsArchived(!current);
        FarmProfile saved = farmProfileRepository.save(profile);
        return mapToResponse(saved);
    }

    @Transactional
    public FarmProfileResponse requestVerification(String farmerEmail) {
        User farmer = authService.getUserByEmail(farmerEmail);
        FarmProfile profile = farmProfileRepository.findByFarmer(farmer)
                .orElseThrow(() -> new BusinessException("Please complete and save your farm profile before requesting verification."));

        if (profile.getVerificationStatus() == VerificationStatus.VERIFIED) {
            throw new BusinessException("This farm profile is already verified.");
        }

        profile.setVerificationStatus(VerificationStatus.PENDING_VERIFICATION);
        profile.setVerificationNotes("Verification requested on " + LocalDateTime.now());
        FarmProfile saved = farmProfileRepository.save(profile);

        // Notify farmer
        notificationService.createNotification(
                farmer,
                "Verification Requested",
                "Your request for Farmora Verified Farm badge has been submitted for administrative review.",
                "VERIFICATION_PENDING",
                saved.getId()
        );

        return mapToResponse(saved);
    }

    public FarmProfileResponse getFarmProfileByFarmerId(Long farmerId) {
        User farmer = userRepository.findById(farmerId)
                .orElseThrow(() -> new ResourceNotFoundException("Farmer not found with id: " + farmerId));

        FarmProfile profile = farmProfileRepository.findByFarmer(farmer)
                .orElseThrow(() -> new ResourceNotFoundException("Farm profile not found for this farmer."));

        return mapToResponse(profile);
    }

    public FarmProfileResponse getFarmProfileById(Long id) {
        FarmProfile profile = farmProfileRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Farm profile not found with id: " + id));

        return mapToResponse(profile);
    }

    public List<FarmProfileResponse> getFarms(String province, String district, FarmingMethod method, VerificationStatus status) {
        List<FarmProfile> farms = farmProfileRepository.filterFarms(
                (province != null && !province.isBlank()) ? province : null,
                (district != null && !district.isBlank()) ? district : null,
                method,
                status
        );

        return farms.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public FarmProfileResponse adminUpdateVerificationStatus(Long farmId, VerificationStatus status, String notes) {
        FarmProfile profile = farmProfileRepository.findById(farmId)
                .orElseThrow(() -> new ResourceNotFoundException("Farm profile not found with id: " + farmId));

        profile.setVerificationStatus(status);
        profile.setVerificationNotes(notes);
        if (status == VerificationStatus.VERIFIED) {
            profile.setVerifiedAt(LocalDateTime.now());
        }

        FarmProfile saved = farmProfileRepository.save(profile);

        String message = status == VerificationStatus.VERIFIED
                ? "Congratulations! Your farm '" + profile.getFarmName() + "' has been approved with the Farmora Verified Farm badge!"
                : "Your verification request for '" + profile.getFarmName() + "' was updated: " + status + (notes != null ? " (" + notes + ")" : "");

        notificationService.createNotification(
                profile.getFarmer(),
                "Farm Verification Status Update",
                message,
                "VERIFICATION_UPDATE",
                saved.getId()
        );

        return mapToResponse(saved);
    }

    public FarmProfileResponse mapToResponse(FarmProfile profile) {
        User farmer = profile.getFarmer();

        // Calculate rating
        List<Review> reviews = reviewRepository.findByTargetUserOrderByCreatedAtDesc(farmer);
        long count = reviews.size();
        double avg = 0.0;
        if (count > 0) {
            double sum = reviews.stream().mapToInt(r -> r.getRating() != null ? r.getRating() : 5).sum();
            avg = Math.round((sum / count) * 10.0) / 10.0;
        }

        return FarmProfileResponse.builder()
                .id(profile.getId())
                .farmer(authService.mapToUserResponse(farmer))
                .farmName(profile.getFarmName())
                .farmerDetails(profile.getFarmerDetails())
                .contactPhone(profile.getContactPhone())
                .contactEmail(profile.getContactEmail())
                .socialLink(profile.getSocialLink())
                .latitude(profile.getLatitude())
                .longitude(profile.getLongitude())
                .farmAddress(profile.getFarmAddress())
                .province(profile.getProvince())
                .district(profile.getDistrict())
                .photoUrls(profile.getPhotoUrls())
                .videoUrl(profile.getVideoUrl())
                .farmingExperience(profile.getFarmingExperience())
                .farmSize(profile.getFarmSize())
                .farmSizeUnit(profile.getFarmSizeUnit())
                .farmingMethod(profile.getFarmingMethod())
                .coverImageUrl(profile.getCoverImageUrl())
                .tagline(profile.getTagline())
                .quote(profile.getQuote())
                .primaryCrops(profile.getPrimaryCrops())
                .farmType(profile.getFarmType())
                .establishedYear(profile.getEstablishedYear())
                .specialization(profile.getSpecialization())
                .skills(profile.getSkills())
                .farmingMethodsList(profile.getFarmingMethodsList())
                .nicNumber(profile.getNicNumber())
                .dateOfBirth(profile.getDateOfBirth())
                .isArchived(profile.getIsArchived() != null ? profile.getIsArchived() : false)
                .archivedSections(profile.getArchivedSections())
                .certifications(profile.getCertifications())
                .verificationStatus(profile.getVerificationStatus())
                .verificationNotes(profile.getVerificationNotes())
                .verifiedAt(profile.getVerifiedAt())
                .averageRating(avg > 0 ? avg : 4.8)
                .totalReviews(count)
                .createdAt(profile.getCreatedAt())
                .updatedAt(profile.getUpdatedAt())
                .build();
    }
}
