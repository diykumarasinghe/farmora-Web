package com.farmora.service;

import com.farmora.dto.delivery.CreateDeliveryRequest;
import com.farmora.dto.delivery.DeliveryResponse;
import com.farmora.dto.delivery.RedirectDeliveryRequest;
import com.farmora.dto.delivery.UpdateDeliveryLocationRequest;
import com.farmora.exception.AccessDeniedCustomException;
import com.farmora.exception.BusinessException;
import com.farmora.exception.ResourceNotFoundException;
import com.farmora.model.*;
import com.farmora.repository.DeliveryRepository;
import com.farmora.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
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

    public DeliveryResponse getDeliveryByOrderId(Long orderId, String userEmail) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found with id: " + orderId));

        Delivery delivery = deliveryRepository.findByOrder(order)
                .orElseThrow(() -> new ResourceNotFoundException("No delivery dispatch found for order #" + orderId));

        return mapToResponse(delivery);
    }

    @Transactional
    public DeliveryResponse createDeliveryRequest(CreateDeliveryRequest request, String buyerEmail) {
        User buyer = authService.getUserByEmail(buyerEmail);
        Order order = orderRepository.findById(request.getOrderId())
                .orElseThrow(() -> new ResourceNotFoundException("Order not found with id: " + request.getOrderId()));

        if (!order.getBuyer().getId().equals(buyer.getId()) && buyer.getRole() != Role.ADMIN) {
            throw new AccessDeniedCustomException("You can only request delivery for your own orders.");
        }

        Delivery delivery = deliveryRepository.findByOrder(order).orElse(null);
        if (delivery == null) {
            delivery = Delivery.builder()
                    .order(order)
                    .pickupAddress(order.getPickupAddress() != null ? order.getPickupAddress() : (order.getProduct().getLocation() != null ? order.getProduct().getLocation() : "Farm Depot"))
                    .deliveryAddress(request.getDeliveryAddress() != null ? request.getDeliveryAddress() : order.getDeliveryAddress())
                    .recipientPhone(request.getRecipientPhone() != null ? request.getRecipientPhone() : buyer.getPhone())
                    .specialInstructions(request.getSpecialInstructions())
                    .status(DeliveryStatus.AVAILABLE)
                    .deliveryFee(new BigDecimal("350.00"))
                    .build();
        } else {
            if (delivery.getStatus() == DeliveryStatus.DELIVERED) {
                throw new BusinessException("Order has already been delivered.");
            }
            if (request.getDeliveryAddress() != null) delivery.setDeliveryAddress(request.getDeliveryAddress());
            if (request.getRecipientPhone() != null) delivery.setRecipientPhone(request.getRecipientPhone());
            if (request.getSpecialInstructions() != null) delivery.setSpecialInstructions(request.getSpecialInstructions());
            delivery.setStatus(DeliveryStatus.AVAILABLE);
        }

        order.setFulfillmentType("DELIVERY");
        orderRepository.save(order);

        Delivery saved = deliveryRepository.save(delivery);
        return mapToResponse(saved);
    }

    @Transactional
    public DeliveryResponse redirectDelivery(Long id, RedirectDeliveryRequest request, String userEmail) {
        Delivery delivery = deliveryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Delivery not found with id: " + id));

        User user = authService.getUserByEmail(userEmail);
        boolean isBuyer = delivery.getOrder().getBuyer().getId().equals(user.getId());
        boolean isAdmin = user.getRole() == Role.ADMIN;

        if (!isBuyer && !isAdmin) {
            throw new AccessDeniedCustomException("Only the recipient buyer or admin can redirect this delivery.");
        }

        if (delivery.getStatus() == DeliveryStatus.DELIVERED || delivery.getStatus() == DeliveryStatus.CANCELLED) {
            throw new BusinessException("Cannot redirect a delivery that is " + delivery.getStatus());
        }

        delivery.setDeliveryAddress(request.getNewDeliveryAddress());
        delivery.getOrder().setDeliveryAddress(request.getNewDeliveryAddress());
        orderRepository.save(delivery.getOrder());

        if (request.getNewRecipientPhone() != null) {
            delivery.setRecipientPhone(request.getNewRecipientPhone());
        }
        if (request.getInstructions() != null) {
            delivery.setSpecialInstructions(request.getInstructions());
        }

        Delivery saved = deliveryRepository.save(delivery);

        // If driver assigned, notify driver of redirect
        if (delivery.getDeliveryPartner() != null) {
            notificationService.createNotification(
                    delivery.getDeliveryPartner(),
                    "Delivery Destination Redirected",
                    "Order #" + delivery.getOrder().getOrderNumber() + " destination was updated to: " + request.getNewDeliveryAddress(),
                    "DELIVERY_REDIRECTED",
                    saved.getId()
            );
        }

        return mapToResponse(saved);
    }

    @Transactional
    public DeliveryResponse cancelDelivery(Long id, String reason, String userEmail) {
        Delivery delivery = deliveryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Delivery not found with id: " + id));

        User user = authService.getUserByEmail(userEmail);
        boolean isBuyer = delivery.getOrder().getBuyer().getId().equals(user.getId());
        boolean isDriver = delivery.getDeliveryPartner() != null && delivery.getDeliveryPartner().getId().equals(user.getId());
        boolean isAdmin = user.getRole() == Role.ADMIN;

        if (!isBuyer && !isDriver && !isAdmin) {
            throw new AccessDeniedCustomException("You are not authorized to cancel this delivery.");
        }

        if (delivery.getStatus() == DeliveryStatus.DELIVERED) {
            throw new BusinessException("Cannot cancel a completed delivery.");
        }

        delivery.setStatus(DeliveryStatus.CANCELLED);
        delivery.setTrackingNotes("Cancelled: " + (reason != null ? reason : "Requested by " + user.getRole()));

        Delivery saved = deliveryRepository.save(delivery);
        return mapToResponse(saved);
    }

    @Transactional
    public DeliveryResponse updateLocation(Long id, UpdateDeliveryLocationRequest request, String partnerEmail) {
        Delivery delivery = deliveryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Delivery not found with id: " + id));

        User partner = authService.getUserByEmail(partnerEmail);
        boolean isAssigned = delivery.getDeliveryPartner() != null && delivery.getDeliveryPartner().getId().equals(partner.getId());
        boolean isAdmin = partner.getRole() == Role.ADMIN;

        if (!isAssigned && !isAdmin) {
            throw new AccessDeniedCustomException("Only the assigned delivery partner can update tracking coordinates.");
        }

        if (request.getCurrentLat() != null) delivery.setCurrentLat(request.getCurrentLat());
        if (request.getCurrentLng() != null) delivery.setCurrentLng(request.getCurrentLng());
        if (request.getTrackingNotes() != null) delivery.setTrackingNotes(request.getTrackingNotes());

        return mapToResponse(deliveryRepository.save(delivery));
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
        delivery.setEstimatedDeliveryTime("Within 2 hours");

        // Seed initial GPS coordinates around Colombo / Kandy region for live simulation
        delivery.setCurrentLat(6.9271);
        delivery.setCurrentLng(79.8612);

        Delivery saved = deliveryRepository.save(delivery);

        // Notify Buyer and Farmer
        notificationService.createNotification(
                delivery.getOrder().getBuyer(),
                "Delivery Driver Assigned!",
                "Partner " + partner.getFullName() + " has accepted your delivery for order #" + (delivery.getOrder().getOrderNumber() != null ? delivery.getOrder().getOrderNumber() : delivery.getOrder().getId()) + ".",
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

        if (newStatus == DeliveryStatus.PICKED_UP) {
            delivery.setPickedUpAt(LocalDateTime.now());
            delivery.setStatus(DeliveryStatus.PICKED_UP);
            delivery.getOrder().setStatus(OrderStatus.OUT_FOR_DELIVERY);
            delivery.setEstimatedDeliveryTime("In transit — ETA ~45 mins");
        } else if (newStatus == DeliveryStatus.IN_TRANSIT) {
            delivery.setStatus(DeliveryStatus.IN_TRANSIT);
            delivery.getOrder().setStatus(OrderStatus.OUT_FOR_DELIVERY);
            delivery.setEstimatedDeliveryTime("Approaching destination — ETA ~15 mins");
        } else if (newStatus == DeliveryStatus.DELIVERED) {
            delivery.setDeliveredAt(LocalDateTime.now());
            delivery.setStatus(DeliveryStatus.DELIVERED);
            delivery.setEstimatedDeliveryTime("Delivered successfully");
            delivery.getOrder().setStatus(OrderStatus.DELIVERED);

            notificationService.createNotification(
                    delivery.getOrder().getBuyer(),
                    "Order Delivered!",
                    "Your harvest for order #" + (delivery.getOrder().getOrderNumber() != null ? delivery.getOrder().getOrderNumber() : delivery.getOrder().getId()) + " has arrived! Please review the farmer.",
                    "ORDER_DELIVERED",
                    delivery.getOrder().getId()
            );
        } else if (newStatus == DeliveryStatus.CANCELLED) {
            delivery.setStatus(DeliveryStatus.CANCELLED);
        }

        if (notes != null && !notes.isBlank()) {
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
                .recipientPhone(delivery.getRecipientPhone())
                .specialInstructions(delivery.getSpecialInstructions())
                .currentLat(delivery.getCurrentLat())
                .currentLng(delivery.getCurrentLng())
                .estimatedDeliveryTime(delivery.getEstimatedDeliveryTime())
                .deliveryFee(delivery.getDeliveryFee() != null ? delivery.getDeliveryFee() : new BigDecimal("350.00"))
                .assignedAt(delivery.getAssignedAt())
                .pickedUpAt(delivery.getPickedUpAt())
                .deliveredAt(delivery.getDeliveredAt())
                .createdAt(delivery.getCreatedAt())
                .updatedAt(delivery.getUpdatedAt())
                .build();
    }
}
