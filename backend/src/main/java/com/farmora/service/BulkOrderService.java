package com.farmora.service;

import com.farmora.dto.bulk.BulkOrderResponse;
import com.farmora.dto.bulk.CreateBulkOrderRequest;
import com.farmora.dto.bulk.ModifyBulkQuantityRequest;
import com.farmora.exception.AccessDeniedCustomException;
import com.farmora.exception.BusinessException;
import com.farmora.exception.ResourceNotFoundException;
import com.farmora.model.*;
import com.farmora.repository.BulkOrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BulkOrderService {

    private final BulkOrderRepository bulkOrderRepository;
    private final AuthService authService;
    private final ProductService productService;
    private final NotificationService notificationService;

    @Transactional
    public BulkOrderResponse createBulkOrder(CreateBulkOrderRequest request, String buyerEmail) {
        User buyer = authService.getUserByEmail(buyerEmail);
        if (buyer.getRole() != Role.BUYER) {
            throw new AccessDeniedCustomException("Only registered buyers can place bulk orders.");
        }

        Product product = productService.getProductEntity(request.getProductId());
        String bulkNumber = "BLK-" + (System.currentTimeMillis() % 1000000);
        BigDecimal total = request.getTargetPricePerUnit().multiply(request.getQuantity());

        BulkOrder bulkOrder = BulkOrder.builder()
                .bulkOrderNumber(bulkNumber)
                .product(product)
                .buyer(buyer)
                .farmer(product.getFarmer())
                .quantity(request.getQuantity())
                .targetPricePerUnit(request.getTargetPricePerUnit())
                .totalEstimatedAmount(total)
                .packagingType(request.getPackagingType() != null ? request.getPackagingType() : "Standard Crates")
                .deliveryFrequency(request.getDeliveryFrequency() != null ? request.getDeliveryFrequency() : "One-time Delivery")
                .requiredDate(request.getRequiredDate())
                .specialRequirements(request.getSpecialRequirements())
                .fulfillmentType(request.getFulfillmentType() != null ? request.getFulfillmentType() : "DELIVERY")
                .destinationAddress(request.getDestinationAddress() != null ? request.getDestinationAddress() : buyer.getAddress())
                .status(BulkOrderStatus.PENDING_QUOTE)
                .build();

        BulkOrder saved = bulkOrderRepository.save(bulkOrder);

        // Notify Farmer
        notificationService.createNotification(
                product.getFarmer(),
                "New Bulk Order Request!",
                "Buyer " + buyer.getFullName() + " requested a wholesale bulk order of " + saved.getQuantity() + " " + product.getUnit() + " of " + product.getName() + " (#" + saved.getBulkOrderNumber() + ").",
                "BULK_ORDER_RECEIVED",
                saved.getId()
        );

        return mapToResponse(saved);
    }

    public List<BulkOrderResponse> getMyBulkOrders(String userEmail) {
        User user = authService.getUserByEmail(userEmail);
        if (user.getRole() == Role.BUYER) {
            return bulkOrderRepository.findByBuyerOrderByCreatedAtDesc(user).stream()
                    .map(this::mapToResponse)
                    .collect(Collectors.toList());
        } else if (user.getRole() == Role.FARMER) {
            return bulkOrderRepository.findByFarmerOrderByCreatedAtDesc(user).stream()
                    .map(this::mapToResponse)
                    .collect(Collectors.toList());
        } else if (user.getRole() == Role.ADMIN) {
            return bulkOrderRepository.findAllByOrderByCreatedAtDesc().stream()
                    .map(this::mapToResponse)
                    .collect(Collectors.toList());
        }
        return List.of();
    }

    public BulkOrderResponse getBulkOrderById(Long id, String userEmail) {
        BulkOrder bulkOrder = bulkOrderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Bulk order not found with id: " + id));

        User user = authService.getUserByEmail(userEmail);
        boolean isBuyer = bulkOrder.getBuyer().getId().equals(user.getId());
        boolean isFarmer = bulkOrder.getFarmer().getId().equals(user.getId());
        boolean isAdmin = user.getRole() == Role.ADMIN;

        if (!isBuyer && !isFarmer && !isAdmin) {
            throw new AccessDeniedCustomException("You are not authorized to view this bulk order.");
        }

        return mapToResponse(bulkOrder);
    }

    @Transactional
    public BulkOrderResponse modifyBulkQuantity(Long id, ModifyBulkQuantityRequest request, String userEmail) {
        BulkOrder bulkOrder = bulkOrderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Bulk order not found with id: " + id));

        User user = authService.getUserByEmail(userEmail);
        boolean isBuyer = bulkOrder.getBuyer().getId().equals(user.getId());
        boolean isAdmin = user.getRole() == Role.ADMIN;

        if (!isBuyer && !isAdmin) {
            throw new AccessDeniedCustomException("Only the buyer can modify this bulk order.");
        }

        if (bulkOrder.getStatus() != BulkOrderStatus.PENDING_QUOTE) {
            throw new BusinessException("Cannot modify bulk order once it is " + bulkOrder.getStatus());
        }

        bulkOrder.setQuantity(request.getQuantity());
        if (request.getTargetPricePerUnit() != null) {
            bulkOrder.setTargetPricePerUnit(request.getTargetPricePerUnit());
        }
        if (request.getSpecialRequirements() != null) {
            bulkOrder.setSpecialRequirements(request.getSpecialRequirements());
        }

        BigDecimal total = bulkOrder.getTargetPricePerUnit().multiply(bulkOrder.getQuantity());
        bulkOrder.setTotalEstimatedAmount(total);

        BulkOrder saved = bulkOrderRepository.save(bulkOrder);

        // Notify Farmer
        notificationService.createNotification(
                bulkOrder.getFarmer(),
                "Bulk Order Modified",
                "Buyer " + user.getFullName() + " updated quantities for Bulk Order #" + bulkOrder.getBulkOrderNumber() + " to " + saved.getQuantity() + " " + bulkOrder.getProduct().getUnit() + ".",
                "BULK_ORDER_MODIFIED",
                saved.getId()
        );

        return mapToResponse(saved);
    }

    @Transactional
    public BulkOrderResponse updateStatus(Long id, BulkOrderStatus newStatus, String notes, String userEmail) {
        BulkOrder bulkOrder = bulkOrderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Bulk order not found with id: " + id));

        User user = authService.getUserByEmail(userEmail);
        boolean isFarmer = bulkOrder.getFarmer().getId().equals(user.getId());
        boolean isAdmin = user.getRole() == Role.ADMIN;

        if (!isFarmer && !isAdmin) {
            throw new AccessDeniedCustomException("Only the producer or admin can update this bulk order status.");
        }

        bulkOrder.setStatus(newStatus);
        if (notes != null) {
            if (newStatus == BulkOrderStatus.REJECTED) {
                bulkOrder.setRejectionOrCancellationReason(notes);
            } else {
                bulkOrder.setFarmerNotes(notes);
            }
        }

        BulkOrder saved = bulkOrderRepository.save(bulkOrder);

        // Notify Buyer
        notificationService.createNotification(
                bulkOrder.getBuyer(),
                "Bulk Order Update: " + newStatus,
                "Farmer " + user.getFullName() + " marked your Bulk Order #" + bulkOrder.getBulkOrderNumber() + " as " + newStatus + ".",
                "BULK_ORDER_STATUS",
                saved.getId()
        );

        return mapToResponse(saved);
    }

    @Transactional
    public BulkOrderResponse cancelBulkOrder(Long id, String reason, String userEmail) {
        BulkOrder bulkOrder = bulkOrderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Bulk order not found with id: " + id));

        User user = authService.getUserByEmail(userEmail);
        boolean isBuyer = bulkOrder.getBuyer().getId().equals(user.getId());
        boolean isFarmer = bulkOrder.getFarmer().getId().equals(user.getId());
        boolean isAdmin = user.getRole() == Role.ADMIN;

        if (!isBuyer && !isFarmer && !isAdmin) {
            throw new AccessDeniedCustomException("You are not authorized to cancel this bulk order.");
        }

        if (bulkOrder.getStatus() == BulkOrderStatus.COMPLETED) {
            throw new BusinessException("Completed bulk orders cannot be cancelled.");
        }

        bulkOrder.setStatus(BulkOrderStatus.CANCELLED);
        bulkOrder.setRejectionOrCancellationReason(reason != null ? reason : "Cancelled by " + user.getRole());

        BulkOrder saved = bulkOrderRepository.save(bulkOrder);

        User recipient = isBuyer ? bulkOrder.getFarmer() : bulkOrder.getBuyer();
        notificationService.createNotification(
                recipient,
                "Bulk Order Cancelled",
                "Bulk Order #" + bulkOrder.getBulkOrderNumber() + " was cancelled. Reason: " + saved.getRejectionOrCancellationReason(),
                "BULK_ORDER_CANCELLED",
                saved.getId()
        );

        return mapToResponse(saved);
    }

    public BulkOrderResponse mapToResponse(BulkOrder b) {
        return BulkOrderResponse.builder()
                .id(b.getId())
                .bulkOrderNumber(b.getBulkOrderNumber())
                .product(productService.mapToResponse(b.getProduct()))
                .buyer(authService.mapToUserResponse(b.getBuyer()))
                .farmer(authService.mapToUserResponse(b.getFarmer()))
                .quantity(b.getQuantity())
                .targetPricePerUnit(b.getTargetPricePerUnit())
                .totalEstimatedAmount(b.getTotalEstimatedAmount())
                .packagingType(b.getPackagingType())
                .deliveryFrequency(b.getDeliveryFrequency())
                .requiredDate(b.getRequiredDate())
                .specialRequirements(b.getSpecialRequirements())
                .fulfillmentType(b.getFulfillmentType())
                .destinationAddress(b.getDestinationAddress())
                .status(b.getStatus())
                .rejectionOrCancellationReason(b.getRejectionOrCancellationReason())
                .farmerNotes(b.getFarmerNotes())
                .createdAt(b.getCreatedAt())
                .updatedAt(b.getUpdatedAt())
                .build();
    }
}
