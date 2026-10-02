package com.farmora.service;

import com.farmora.dto.farm.FarmProfileResponse;
import com.farmora.exception.AccessDeniedCustomException;
import com.farmora.exception.BusinessException;
import com.farmora.exception.ResourceNotFoundException;
import com.farmora.model.FavouriteFarm;
import com.farmora.model.Role;
import com.farmora.model.User;
import com.farmora.repository.FavouriteFarmRepository;
import com.farmora.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class FavouriteFarmService {

    private final FavouriteFarmRepository favouriteFarmRepository;
    private final UserRepository userRepository;
    private final AuthService authService;
    private final FarmProfileService farmProfileService;

    public List<FarmProfileResponse> getFavouriteFarms(String buyerEmail) {
        User buyer = authService.getUserByEmail(buyerEmail);
        List<FavouriteFarm> favourites = favouriteFarmRepository.findByBuyerOrderByCreatedAtDesc(buyer);

        return favourites.stream()
                .map(f -> {
                    try {
                        return farmProfileService.getFarmProfileByFarmerId(f.getFarmer().getId());
                    } catch (Exception e) {
                        // If no dedicated farm profile exists yet, create fallback response
                        return FarmProfileResponse.builder()
                                .farmer(authService.mapToUserResponse(f.getFarmer()))
                                .farmName(f.getFarmer().getFullName() + "'s Farm")
                                .farmAddress(f.getFarmer().getAddress())
                                .contactPhone(f.getFarmer().getPhone())
                                .contactEmail(f.getFarmer().getEmail())
                                .build();
                    }
                })
                .collect(Collectors.toList());
    }

    @Transactional
    public void addFavourite(Long farmerId, String buyerEmail) {
        User buyer = authService.getUserByEmail(buyerEmail);
        if (buyer.getRole() != Role.BUYER) {
            throw new AccessDeniedCustomException("Only buyers can save favourite farms.");
        }

        User farmer = userRepository.findById(farmerId)
                .orElseThrow(() -> new ResourceNotFoundException("Farmer not found with id: " + farmerId));

        if (farmer.getRole() != Role.FARMER) {
            throw new BusinessException("Selected user is not a registered farmer.");
        }

        if (favouriteFarmRepository.existsByBuyerAndFarmer(buyer, farmer)) {
            return; // Already favourite
        }

        FavouriteFarm favourite = FavouriteFarm.builder()
                .buyer(buyer)
                .farmer(farmer)
                .build();

        favouriteFarmRepository.save(favourite);
    }

    @Transactional
    public void removeFavourite(Long farmerId, String buyerEmail) {
        User buyer = authService.getUserByEmail(buyerEmail);
        User farmer = userRepository.findById(farmerId)
                .orElseThrow(() -> new ResourceNotFoundException("Farmer not found with id: " + farmerId));

        favouriteFarmRepository.deleteByBuyerAndFarmer(buyer, farmer);
    }

    public boolean isFavourite(Long farmerId, String buyerEmail) {
        User buyer = authService.getUserByEmail(buyerEmail);
        User farmer = userRepository.findById(farmerId).orElse(null);
        if (farmer == null) return false;
        return favouriteFarmRepository.existsByBuyerAndFarmer(buyer, farmer);
    }
}
