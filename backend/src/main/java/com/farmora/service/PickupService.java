package com.farmora.service;

import com.farmora.dto.pickup.PickupResponse;
import com.farmora.dto.pickup.ReschedulePickupRequest;
import com.farmora.dto.pickup.SchedulePickupRequest;
import com.farmora.exception.AccessDeniedCustomException;
import com.farmora.exception.BusinessException;
import com.farmora.exception.ResourceNotFoundException;
import com.farmora.model.*;
import com.farmora.repository.OrderRepository;
import com.farmora.repository.PickupRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PickupService {

    private final PickupRepository pickupRepository;
    private final OrderRepository orderRepository;
    private final AuthService authService;
    private final OrderService orderService;
    private final NotificationService notificationService;

    @Transactional
    public PickupResponse schedulePickup(SchedulePickupRequest request, String buyerEmail) {
        User buyer = authService.getUserByEmail(buyerEmail);
        Order order = orderRepository.findById(request.getOrderId())
                .orElseThrow(() -> new ResourceNotFoundException("Order not found with id: " + request.getOrderId()));

        if (!order.getBuyer().getId().equals(buyer.getId()) && buyer.getRole() != Role.ADMIN) {
            throw new AccessDeniedCustomException("You can only schedule pickup for your own orders.");
        }

        if (order.getStatus() == OrderStatus.CANCELLED || order.getStatus() == OrderStatus.DELIVERED) {
            throw new BusinessException("Cannot schedule pickup for an order that is " + order.getStatus());
        }

        // Check if an appointment already exists for this order
        PickupAppointment appointment = pickupRepository.findByOrder(order).orElse(null);
        if (appointment == null) {
            String passcode = "PK-" + (10000 + new Random().nextInt(90000));
            appointment = PickupAppointment.builder()
                    .order(order)
                    .buyer(buyer)
                    .farmer(order.getFarmer())
                    .pickupDate(request.getPickupDate())
                    .timeSlot(request.getTimeSlot())
                    .vehicleNumber(request.getVehicleNumber())
                    .contactPhone(request.getContactPhone() != null ? request.getContactPhone() : buyer.getPhone())
                    .passcode(passcode)
                    .status(PickupStatus.SCHEDULED)
                    .pickupLocation(order.getPickupAddress() != null ? order.getPickupAddress() : (order.getProduct().getLocation() != null ? order.getProduct().getLocation() : "Farm Gate Depot"))
                    .instructions(request.getInstructions())
                    .build();
        } else {
            appointment.setPickupDate(request.getPickupDate());
            appointment.setTimeSlot(request.getTimeSlot());
            appointment.setVehicleNumber(request.getVehicleNumber());
            if (request.getContactPhone() != null) appointment.setContactPhone(request.getContactPhone());
            if (request.getInstructions() != null) appointment.setInstructions(request.getInstructions());
            appointment.setStatus(PickupStatus.SCHEDULED);
        }

        // Ensure order fulfillment is set to PICKUP
        order.setFulfillmentType("PICKUP");
        orderRepository.save(order);

        PickupAppointment saved = pickupRepository.save(appointment);

        // Notify farmer
        notificationService.createNotification(
                order.getFarmer(),
                "Farm Pickup Scheduled",
                "Buyer " + buyer.getFullName() + " scheduled farm pickup for Order #" + (order.getOrderNumber() != null ? order.getOrderNumber() : order.getId()) + " on " + saved.getPickupDate() + " (" + saved.getTimeSlot() + ").",
                "PICKUP_SCHEDULED",
                saved.getId()
        );

        return mapToResponse(saved);
    }

    public List<PickupResponse> getMyPickups(String userEmail) {
        User user = authService.getUserByEmail(userEmail);
        if (user.getRole() == Role.BUYER) {
            return pickupRepository.findByBuyerOrderByCreatedAtDesc(user).stream()
                    .map(this::mapToResponse)
                    .collect(Collectors.toList());
        } else if (user.getRole() == Role.FARMER) {
            return pickupRepository.findByFarmerOrderByCreatedAtDesc(user).stream()
                    .map(this::mapToResponse)
                    .collect(Collectors.toList());
        } else if (user.getRole() == Role.ADMIN) {
            return pickupRepository.findAll().stream()
                    .map(this::mapToResponse)
                    .collect(Collectors.toList());
        }
        return List.of();
    }

    public PickupResponse getPickupByOrderId(Long orderId, String userEmail) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found with id: " + orderId));

        User user = authService.getUserByEmail(userEmail);
        boolean isBuyer = order.getBuyer().getId().equals(user.getId());
        boolean isFarmer = order.getFarmer().getId().equals(user.getId());
        boolean isAdmin = user.getRole() == Role.ADMIN;

        if (!isBuyer && !isFarmer && !isAdmin) {
            throw new AccessDeniedCustomException("You are not authorized to view pickup for this order.");
        }

        PickupAppointment appointment = pickupRepository.findByOrder(order)
                .orElseThrow(() -> new ResourceNotFoundException("No pickup appointment scheduled for order id: " + orderId));

        return mapToResponse(appointment);
    }

    @Transactional
    public PickupResponse reschedulePickup(Long id, ReschedulePickupRequest request, String userEmail) {
        PickupAppointment appointment = pickupRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Pickup appointment not found with id: " + id));

        User user = authService.getUserByEmail(userEmail);
        boolean isBuyer = appointment.getBuyer().getId().equals(user.getId());
        boolean isFarmer = appointment.getFarmer().getId().equals(user.getId());
        boolean isAdmin = user.getRole() == Role.ADMIN;

        if (!isBuyer && !isFarmer && !isAdmin) {
            throw new AccessDeniedCustomException("You are not authorized to reschedule this appointment.");
        }

        if (appointment.getStatus() == PickupStatus.COMPLETED || appointment.getStatus() == PickupStatus.CANCELLED) {
            throw new BusinessException("Cannot reschedule a " + appointment.getStatus() + " pickup.");
        }

        appointment.setPickupDate(request.getPickupDate());
        appointment.setTimeSlot(request.getTimeSlot());
        appointment.setRescheduledReason(request.getReason());
        appointment.setStatus(PickupStatus.RESCHEDULED);

        PickupAppointment saved = pickupRepository.save(appointment);

        // Notify other party
        User recipient = isBuyer ? appointment.getFarmer() : appointment.getBuyer();
        notificationService.createNotification(
                recipient,
                "Pickup Rescheduled",
                "Farm pickup for order #" + (appointment.getOrder().getOrderNumber() != null ? appointment.getOrder().getOrderNumber() : appointment.getOrder().getId()) + " was rescheduled to " + saved.getPickupDate() + " (" + saved.getTimeSlot() + ").",
                "PICKUP_RESCHEDULED",
                saved.getId()
        );

        return mapToResponse(saved);
    }

    @Transactional
    public PickupResponse cancelPickup(Long id, String reason, String userEmail) {
        PickupAppointment appointment = pickupRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Pickup appointment not found with id: " + id));

        User user = authService.getUserByEmail(userEmail);
        boolean isBuyer = appointment.getBuyer().getId().equals(user.getId());
        boolean isFarmer = appointment.getFarmer().getId().equals(user.getId());
        boolean isAdmin = user.getRole() == Role.ADMIN;

        if (!isBuyer && !isFarmer && !isAdmin) {
            throw new AccessDeniedCustomException("You are not authorized to cancel this appointment.");
        }

        if (appointment.getStatus() == PickupStatus.COMPLETED) {
            throw new BusinessException("Cannot cancel a completed pickup.");
        }

        appointment.setStatus(PickupStatus.CANCELLED);
        appointment.setCancellationReason(reason != null ? reason : "Cancelled by " + user.getRole());

        PickupAppointment saved = pickupRepository.save(appointment);

        User recipient = isBuyer ? appointment.getFarmer() : appointment.getBuyer();
        notificationService.createNotification(
                recipient,
                "Pickup Cancelled",
                "Pickup appointment for order #" + (appointment.getOrder().getOrderNumber() != null ? appointment.getOrder().getOrderNumber() : appointment.getOrder().getId()) + " has been cancelled.",
                "PICKUP_CANCELLED",
                saved.getId()
        );

        return mapToResponse(saved);
    }

    @Transactional
    public PickupResponse updatePickupStatus(Long id, PickupStatus newStatus, String verificationPasscode, String userEmail) {
        PickupAppointment appointment = pickupRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Pickup appointment not found with id: " + id));

        User user = authService.getUserByEmail(userEmail);
        boolean isFarmer = appointment.getFarmer().getId().equals(user.getId());
        boolean isAdmin = user.getRole() == Role.ADMIN;

        if (!isFarmer && !isAdmin) {
            throw new AccessDeniedCustomException("Only the farmer can verify and complete pickup status.");
        }

        if (newStatus == PickupStatus.COMPLETED) {
            // Validate verification passcode if provided
            if (verificationPasscode != null && !verificationPasscode.isBlank()) {
                if (!appointment.getPasscode().equalsIgnoreCase(verificationPasscode.trim())) {
                    throw new BusinessException("Invalid pickup passcode. Please verify with the buyer.");
                }
            }
            appointment.setStatus(PickupStatus.COMPLETED);
            appointment.getOrder().setStatus(OrderStatus.DELIVERED);
            orderRepository.save(appointment.getOrder());

            notificationService.createNotification(
                    appointment.getBuyer(),
                    "Pickup Completed Successfully!",
                    "Your farm pickup for order #" + (appointment.getOrder().getOrderNumber() != null ? appointment.getOrder().getOrderNumber() : appointment.getOrder().getId()) + " was completed. Enjoy your harvest!",
                    "PICKUP_COMPLETED",
                    appointment.getId()
            );
        } else {
            appointment.setStatus(newStatus);
        }

        return mapToResponse(pickupRepository.save(appointment));
    }

    public PickupResponse mapToResponse(PickupAppointment a) {
        return PickupResponse.builder()
                .id(a.getId())
                .order(orderService.mapToOrderResponse(a.getOrder()))
                .buyer(authService.mapToUserResponse(a.getBuyer()))
                .farmer(authService.mapToUserResponse(a.getFarmer()))
                .pickupDate(a.getPickupDate())
                .timeSlot(a.getTimeSlot())
                .vehicleNumber(a.getVehicleNumber())
                .contactPhone(a.getContactPhone())
                .passcode(a.getPasscode())
                .status(a.getStatus())
                .pickupLocation(a.getPickupLocation())
                .instructions(a.getInstructions())
                .cancellationReason(a.getCancellationReason())
                .rescheduledReason(a.getRescheduledReason())
                .createdAt(a.getCreatedAt())
                .updatedAt(a.getUpdatedAt())
                .build();
    }
}
