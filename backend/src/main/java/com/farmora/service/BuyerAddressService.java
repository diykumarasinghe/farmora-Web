package com.farmora.service;

import com.farmora.dto.buyer.BuyerAddressRequest;
import com.farmora.dto.buyer.BuyerAddressResponse;
import com.farmora.exception.AccessDeniedCustomException;
import com.farmora.exception.ResourceNotFoundException;
import com.farmora.model.BuyerAddress;
import com.farmora.model.Role;
import com.farmora.model.User;
import com.farmora.repository.BuyerAddressRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BuyerAddressService {

    private final BuyerAddressRepository buyerAddressRepository;
    private final AuthService authService;

    public List<BuyerAddressResponse> getMyAddresses(String buyerEmail) {
        User buyer = authService.getUserByEmail(buyerEmail);
        return buyerAddressRepository.findByBuyerOrderByIsDefaultDescCreatedAtDesc(buyer).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public BuyerAddressResponse addAddress(BuyerAddressRequest request, String buyerEmail) {
        User buyer = authService.getUserByEmail(buyerEmail);
        if (buyer.getRole() != Role.BUYER) {
            throw new AccessDeniedCustomException("Only buyers can manage delivery addresses.");
        }

        List<BuyerAddress> existing = buyerAddressRepository.findByBuyerOrderByIsDefaultDescCreatedAtDesc(buyer);
        boolean shouldBeDefault = existing.isEmpty() || Boolean.TRUE.equals(request.getIsDefault());

        if (shouldBeDefault) {
            existing.forEach(a -> {
                if (Boolean.TRUE.equals(a.getIsDefault())) {
                    a.setIsDefault(false);
                    buyerAddressRepository.save(a);
                }
            });
        }

        BuyerAddress address = BuyerAddress.builder()
                .buyer(buyer)
                .label(request.getLabel().trim())
                .recipientName(request.getRecipientName() != null ? request.getRecipientName().trim() : buyer.getFullName())
                .phone(request.getPhone() != null ? request.getPhone().trim() : buyer.getPhone())
                .streetAddress(request.getStreetAddress().trim())
                .city(request.getCity() != null ? request.getCity().trim() : null)
                .district(request.getDistrict() != null ? request.getDistrict().trim() : null)
                .province(request.getProvince() != null ? request.getProvince().trim() : null)
                .postalCode(request.getPostalCode() != null ? request.getPostalCode().trim() : null)
                .isDefault(shouldBeDefault)
                .build();

        BuyerAddress saved = buyerAddressRepository.save(address);

        // Update default address on buyer if default
        if (shouldBeDefault) {
            buyer.setAddress(saved.getStreetAddress() + (saved.getCity() != null ? ", " + saved.getCity() : ""));
        }

        return mapToResponse(saved);
    }

    @Transactional
    public BuyerAddressResponse updateAddress(Long id, BuyerAddressRequest request, String buyerEmail) {
        User buyer = authService.getUserByEmail(buyerEmail);
        BuyerAddress address = buyerAddressRepository.findByIdAndBuyer(id, buyer)
                .orElseThrow(() -> new ResourceNotFoundException("Delivery address not found."));

        if (Boolean.TRUE.equals(request.getIsDefault()) && !Boolean.TRUE.equals(address.getIsDefault())) {
            List<BuyerAddress> existing = buyerAddressRepository.findByBuyerOrderByIsDefaultDescCreatedAtDesc(buyer);
            existing.forEach(a -> {
                if (Boolean.TRUE.equals(a.getIsDefault())) {
                    a.setIsDefault(false);
                    buyerAddressRepository.save(a);
                }
            });
            address.setIsDefault(true);
        }

        address.setLabel(request.getLabel().trim());
        if (request.getRecipientName() != null) {
            address.setRecipientName(request.getRecipientName().trim());
        }
        if (request.getPhone() != null) {
            address.setPhone(request.getPhone().trim());
        }
        address.setStreetAddress(request.getStreetAddress().trim());
        address.setCity(request.getCity() != null ? request.getCity().trim() : null);
        address.setDistrict(request.getDistrict() != null ? request.getDistrict().trim() : null);
        address.setProvince(request.getProvince() != null ? request.getProvince().trim() : null);
        address.setPostalCode(request.getPostalCode() != null ? request.getPostalCode().trim() : null);

        BuyerAddress saved = buyerAddressRepository.save(address);
        return mapToResponse(saved);
    }

    @Transactional
    public void deleteAddress(Long id, String buyerEmail) {
        User buyer = authService.getUserByEmail(buyerEmail);
        BuyerAddress address = buyerAddressRepository.findByIdAndBuyer(id, buyer)
                .orElseThrow(() -> new ResourceNotFoundException("Delivery address not found."));

        boolean wasDefault = Boolean.TRUE.equals(address.getIsDefault());
        buyerAddressRepository.delete(address);

        // If default was deleted, set next available as default
        if (wasDefault) {
            List<BuyerAddress> remaining = buyerAddressRepository.findByBuyerOrderByIsDefaultDescCreatedAtDesc(buyer);
            if (!remaining.isEmpty()) {
                BuyerAddress next = remaining.get(0);
                next.setIsDefault(true);
                buyerAddressRepository.save(next);
            }
        }
    }

    @Transactional
    public BuyerAddressResponse setDefaultAddress(Long id, String buyerEmail) {
        User buyer = authService.getUserByEmail(buyerEmail);
        BuyerAddress target = buyerAddressRepository.findByIdAndBuyer(id, buyer)
                .orElseThrow(() -> new ResourceNotFoundException("Delivery address not found."));

        List<BuyerAddress> existing = buyerAddressRepository.findByBuyerOrderByIsDefaultDescCreatedAtDesc(buyer);
        existing.forEach(a -> {
            a.setIsDefault(a.getId().equals(target.getId()));
            buyerAddressRepository.save(a);
        });

        target.setIsDefault(true);
        BuyerAddress saved = buyerAddressRepository.save(target);

        buyer.setAddress(saved.getStreetAddress() + (saved.getCity() != null ? ", " + saved.getCity() : ""));

        return mapToResponse(saved);
    }

    public BuyerAddressResponse mapToResponse(BuyerAddress address) {
        return BuyerAddressResponse.builder()
                .id(address.getId())
                .buyerId(address.getBuyer().getId())
                .label(address.getLabel())
                .recipientName(address.getRecipientName())
                .phone(address.getPhone())
                .streetAddress(address.getStreetAddress())
                .city(address.getCity())
                .district(address.getDistrict())
                .province(address.getProvince())
                .postalCode(address.getPostalCode())
                .isDefault(address.getIsDefault())
                .createdAt(address.getCreatedAt())
                .build();
    }
}
