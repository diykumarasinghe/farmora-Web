package com.farmora.service;

import com.farmora.dto.delivery.DeliveryResponse;
import com.farmora.exception.AccessDeniedCustomException;
import com.farmora.exception.BusinessException;
import com.farmora.exception.ResourceNotFoundException;
import com.farmora.model.*;
import com.farmora.repository.DeliveryRepository;
import com.farmora.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DeliveryService {

    private final DeliveryRepository deliveryRepository;
    private final OrderRepository orderRepository;
    private final AuthService authService;
    private final OrderService orderService;
    private final NotificationService notificationService;

    public List<DeliveryResponse> getAvailableDeliveries() {
        return deliveryRepository.findByStatusOrderByCreatedAtDesc(DeliveryStatus.AVAILABLE).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public List<DeliveryResponse> getMyDeliveries(String deliveryPartnerEmail) {
        User partner = authService.getUserByEmail(deliveryPartnerEmail);
        return deliveryRepository.findByDeliveryPartnerOrderByCreatedAtDesc(partner).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public DeliveryResponse getDeliveryById(Long id) {
        Delivery delivery = deliveryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Delivery not found with id: " + id));
        return mapToResponse(delivery);
    }

    @Transactional
    public DeliveryResponse acceptDelivery(Long id, String deliveryPartnerEmail) {
        Delivery delivery = deliveryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Delivery not found with id: " + id));

        if (delivery.getStatus() != DeliveryStatus.AVAILABLE) {
            throw new BusinessException("Delivery is not available. Status: " + delivery.getStatus());
        }

        User partner = authService.getUserByEmail(deliveryPartnerEmail);
        if (partner.getRole() != Role.DELIVERY_PARTNER) {
            throw new AccessDeniedCustomException("Only registered delivery partners can accept deliveries.");
        }

        delivery.setDeliveryPartner(partner);
        delivery.setStatus(DeliveryStatus.ASSIGNED);
        delivery.setAssignedAt(LocalDateTime.now());
        Delivery saved = deliveryRepository.save(delivery);

        // Notify Buyer and Farmer
        notificationService.createNotification(
                delivery.getOrder().getBuyer(),
                "Delivery Partner Assigned",
                "Partner " + partner.getFullName() + " has accepted your delivery for order #" + delivery.getOrder().getId(),
                "DELIVERY_ASSIGNED",
                saved.getId()
        );

        return mapToResponse(saved);
    }

    @Transactional
    public DeliveryResponse updateDeliveryStatus(Long id, DeliveryStatus newStatus, String notes, String deliveryPartnerEmail) {
        Delivery delivery = deliveryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Delivery not found with id: " + id));

        User partner = authService.getUserByEmail(deliveryPartnerEmail);
        boolean isAssigned = delivery.getDeliveryPartner() != null && delivery.getDeliveryPartner().getId().equals(partner.getId());
        boolean isAdmin = partner.getRole() == Role.ADMIN;

        if (!isAssigned && !isAdmin) {
            throw new AccessDeniedCustomException("You are not authorized to update this delivery.");
        }

        // Validate state transitions
        DeliveryStatus current = delivery.getStatus();
        if (newStatus == DeliveryStatus.PICKED_UP) {
            if (current != DeliveryStatus.ASSIGNED) {
                throw new BusinessException("Cannot mark as PICKED_UP from current status: " + current);
            }
            delivery.setPickedUpAt(LocalDateTime.now());
            delivery.setStatus(DeliveryStatus.PICKED_UP);
            delivery.getOrder().setStatus(OrderStatus.OUT_FOR_DELIVERY);
        } else if (newStatus == DeliveryStatus.IN_TRANSIT) {
            if (current != DeliveryStatus.PICKED_UP) {
                throw new BusinessException("Cannot mark as IN_TRANSIT before picking up order.");
            }
            delivery.setStatus(DeliveryStatus.IN_TRANSIT);
            delivery.getOrder().setStatus(OrderStatus.OUT_FOR_DELIVERY);
        } else if (newStatus == DeliveryStatus.DELIVERED) {
            if (current != DeliveryStatus.PICKED_UP && current != DeliveryStatus.IN_TRANSIT) {
                throw new BusinessException("Cannot mark as DELIVERED before pickup or in-transit.");
            }
            delivery.setDeliveredAt(LocalDateTime.now());
            delivery.setStatus(DeliveryStatus.DELIVERED);
            delivery.getOrder().setStatus(OrderStatus.DELIVERED);

            // Notify buyer
            notificationService.createNotification(
                    delivery.getOrder().getBuyer(),
                    "Order Delivered!",
                    "Your order #" + delivery.getOrder().getId() + " has been successfully delivered! Please leave a review.",
                    "ORDER_DELIVERED",
                    delivery.getOrder().getId()
            );
        } else if (newStatus == DeliveryStatus.CANCELLED) {
            delivery.setStatus(DeliveryStatus.CANCELLED);
        }

        if (notes != null) {
            delivery.setTrackingNotes(notes);
        }

        orderRepository.save(delivery.getOrder());
        Delivery saved = deliveryRepository.save(delivery);
        return mapToResponse(saved);
    }

    public DeliveryResponse mapToResponse(Delivery delivery) {
        return DeliveryResponse.builder()
                .id(delivery.getId())
                .order(orderService.mapToOrderResponse(delivery.getOrder()))
                .deliveryPartner(delivery.getDeliveryPartner() != null ? authService.mapToUserResponse(delivery.getDeliveryPartner()) : null)
                .pickupAddress(delivery.getPickupAddress())
                .deliveryAddress(delivery.getDeliveryAddress())
                .status(delivery.getStatus())
                .trackingNotes(delivery.getTrackingNotes())
                .assignedAt(delivery.getAssignedAt())
                .pickedUpAt(delivery.getPickedUpAt())
                .deliveredAt(delivery.getDeliveredAt())
                .createdAt(delivery.getCreatedAt())
                .updatedAt(delivery.getUpdatedAt())
                .build();
    }
}
