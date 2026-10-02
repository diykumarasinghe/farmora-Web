package com.farmora.service;

import com.farmora.dto.order.OrderResponse;
import com.farmora.exception.AccessDeniedCustomException;
import com.farmora.exception.ResourceNotFoundException;
import com.farmora.model.*;
import com.farmora.repository.DeliveryRepository;
import com.farmora.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final DeliveryRepository deliveryRepository;
    private final AuthService authService;
    private final ProductService productService;
    private final NotificationService notificationService;

    public List<OrderResponse> getMyOrders(String userEmail) {
        User user = authService.getUserByEmail(userEmail);
        if (user.getRole() == Role.BUYER) {
            return orderRepository.findByBuyerOrderByCreatedAtDesc(user).stream()
                    .map(this::mapToOrderResponse)
                    .collect(Collectors.toList());
        } else if (user.getRole() == Role.FARMER) {
            return orderRepository.findByFarmerOrderByCreatedAtDesc(user).stream()
                    .map(this::mapToOrderResponse)
                    .collect(Collectors.toList());
        } else if (user.getRole() == Role.ADMIN) {
            return orderRepository.findAllByOrderByCreatedAtDesc().stream()
                    .map(this::mapToOrderResponse)
                    .collect(Collectors.toList());
        }
        return List.of();
    }

    public OrderResponse getOrderById(Long id, String userEmail) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found with id: " + id));

        User user = authService.getUserByEmail(userEmail);
        boolean isBuyer = order.getBuyer().getId().equals(user.getId());
        boolean isFarmer = order.getFarmer().getId().equals(user.getId());
        boolean isAdmin = user.getRole() == Role.ADMIN;
        boolean isDeliveryPartner = user.getRole() == Role.DELIVERY_PARTNER;

        if (!isBuyer && !isFarmer && !isAdmin && !isDeliveryPartner) {
            throw new AccessDeniedCustomException("You are not authorized to view this order.");
        }

        return mapToOrderResponse(order);
    }

    @Transactional
    public OrderResponse updateOrderStatus(Long id, OrderStatus newStatus, String userEmail) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found with id: " + id));

        User user = authService.getUserByEmail(userEmail);
        boolean isFarmer = order.getFarmer().getId().equals(user.getId());
        boolean isAdmin = user.getRole() == Role.ADMIN;

        if (!isFarmer && !isAdmin) {
            throw new AccessDeniedCustomException("Only the farmer or an administrator can update the order status.");
        }

        order.setStatus(newStatus);
        Order saved = orderRepository.save(order);

        // If status changed to READY_FOR_PICKUP, create an AVAILABLE delivery task if one doesn't exist
        if (newStatus == OrderStatus.READY_FOR_PICKUP) {
            deliveryRepository.findByOrder(saved).orElseGet(() -> {
                Delivery delivery = Delivery.builder()
                        .order(saved)
                        .pickupAddress(saved.getPickupAddress())
                        .deliveryAddress(saved.getDeliveryAddress())
                        .status(DeliveryStatus.AVAILABLE)
                        .build();
                return deliveryRepository.save(delivery);
            });

            // Notify buyer
            notificationService.createNotification(
                    saved.getBuyer(),
                    "Order Ready For Pickup",
                    "Your order #" + saved.getId() + " is packaged and ready for delivery pickup!",
                    "ORDER_READY_FOR_PICKUP",
                    saved.getId()
            );
        }

        return mapToOrderResponse(saved);
    }

    public Order getOrderEntity(Long id) {
        return orderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found with id: " + id));
    }

    public OrderResponse mapToOrderResponse(Order order) {
        return OrderResponse.builder()
                .id(order.getId())
                .bidId(order.getBid() != null ? order.getBid().getId() : null)
                .buyer(authService.mapToUserResponse(order.getBuyer()))
                .farmer(authService.mapToUserResponse(order.getFarmer()))
                .product(productService.mapToResponse(order.getProduct()))
                .quantity(order.getQuantity())
                .unitPrice(order.getUnitPrice())
                .totalAmount(order.getTotalAmount())
                .status(order.getStatus())
                .deliveryAddress(order.getDeliveryAddress())
                .pickupAddress(order.getPickupAddress())
                .createdAt(order.getCreatedAt())
                .updatedAt(order.getUpdatedAt())
                .build();
    }
}
