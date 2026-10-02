package com.farmora.service;

import com.farmora.dto.order.CancelOrderRequest;
import com.farmora.dto.order.CreateOrderRequest;
import com.farmora.dto.order.OrderResponse;
import com.farmora.exception.AccessDeniedCustomException;
import com.farmora.exception.BusinessException;
import com.farmora.exception.ResourceNotFoundException;
import com.farmora.model.*;
import com.farmora.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final DeliveryRepository deliveryRepository;
    private final PickupRepository pickupRepository;
    private final InvoiceRepository invoiceRepository;
    private final PaymentRepository paymentRepository;
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
    public OrderResponse createDirectOrder(CreateOrderRequest request, String buyerEmail) {
        User buyer = authService.getUserByEmail(buyerEmail);
        if (buyer.getRole() != Role.BUYER) {
            throw new AccessDeniedCustomException("Only registered buyers can place direct orders.");
        }

        Product product = productService.getProductEntity(request.getProductId());
        if (product.getStatus() != ProductStatus.AVAILABLE) {
            throw new BusinessException("Product is not currently available for orders.");
        }

        if (request.getQuantity().compareTo(product.getQuantity()) > 0) {
            throw new BusinessException("Order quantity exceeds available stock (" + product.getQuantity() + " " + product.getUnit() + ").");
        }

        // Deduct inventory
        BigDecimal remainingQty = product.getQuantity().subtract(request.getQuantity());
        product.setQuantity(remainingQty);
        if (remainingQty.compareTo(BigDecimal.ZERO) <= 0) {
            product.setStatus(ProductStatus.SOLD_OUT);
        }
        productService.saveProduct(product);

        BigDecimal unitPrice = request.getUnitPrice() != null ? request.getUnitPrice() : product.getPrice();
        BigDecimal totalAmount = unitPrice.multiply(request.getQuantity());

        String fulfillmentType = request.getFulfillmentType() != null && request.getFulfillmentType().equalsIgnoreCase("PICKUP") ? "PICKUP" : "DELIVERY";
        String paymentMethod = request.getPaymentMethod() != null ? request.getPaymentMethod().toUpperCase() : "CARD";
        String paymentStatus = paymentMethod.equals("CASH_ON_DELIVERY") ? "UNPAID" : "PAID";

        String orderNum = "ORD-" + System.currentTimeMillis() % 1000000;

        Order order = Order.builder()
                .orderNumber(orderNum)
                .buyer(buyer)
                .farmer(product.getFarmer())
                .product(product)
                .quantity(request.getQuantity())
                .unitPrice(unitPrice)
                .totalAmount(totalAmount)
                .status(OrderStatus.CONFIRMED)
                .fulfillmentType(fulfillmentType)
                .paymentStatus(paymentStatus)
                .paymentMethod(paymentMethod)
                .deliveryAddress(fulfillmentType.equals("DELIVERY") ? (request.getDeliveryAddress() != null ? request.getDeliveryAddress() : buyer.getAddress()) : null)
                .pickupAddress(fulfillmentType.equals("PICKUP") ? (product.getLocation() != null ? product.getLocation() : product.getFarmer().getAddress()) : null)
                .notes(request.getNotes())
                .build();

        Order saved = orderRepository.save(order);

        // Auto create Delivery if DELIVERY
        if ("DELIVERY".equalsIgnoreCase(fulfillmentType)) {
            Delivery delivery = Delivery.builder()
                    .order(saved)
                    .pickupAddress(product.getLocation() != null ? product.getLocation() : product.getFarmer().getAddress())
                    .deliveryAddress(saved.getDeliveryAddress() != null ? saved.getDeliveryAddress() : "Standard Delivery Address")
                    .status(DeliveryStatus.AVAILABLE)
                    .recipientPhone(buyer.getPhone())
                    .specialInstructions(request.getNotes())
                    .deliveryFee(new BigDecimal("350.00"))
                    .build();
            deliveryRepository.save(delivery);
        }

        // Auto create initial Invoice
        String invNum = "INV-" + (System.currentTimeMillis() % 1000000);
        BigDecimal deliveryFee = "DELIVERY".equalsIgnoreCase(fulfillmentType) ? new BigDecimal("350.00") : BigDecimal.ZERO;
        BigDecimal grandTotal = totalAmount.add(deliveryFee);

        Invoice invoice = Invoice.builder()
                .invoiceNumber(invNum)
                .order(saved)
                .buyer(buyer)
                .farmer(product.getFarmer())
                .issueDate(LocalDateTime.now())
                .dueDate(LocalDateTime.now().plusDays(7))
                .subtotal(totalAmount)
                .taxAmount(BigDecimal.ZERO)
                .deliveryFee(deliveryFee)
                .discountAmount(BigDecimal.ZERO)
                .totalAmount(grandTotal)
                .billingName(buyer.getFullName())
                .billingAddress(saved.getDeliveryAddress() != null ? saved.getDeliveryAddress() : buyer.getAddress())
                .taxId("TIN-" + (buyer.getId() + 10000))
                .businessNotes("Thank you for choosing Farmora fresh harvest.")
                .paymentStatus(paymentStatus)
                .isArchived(false)
                .build();
        invoiceRepository.save(invoice);

        // Create Payment record if PAID
        if ("PAID".equals(paymentStatus)) {
            String txnRef = "TXN-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
            Payment payment = Payment.builder()
                    .transactionReference(txnRef)
                    .order(saved)
                    .buyer(buyer)
                    .amount(grandTotal)
                    .paymentMethod(paymentMethod)
                    .status(PaymentStatus.COMPLETED)
                    .cardLast4("4242")
                    .cardBrand("VISA")
                    .paidAt(LocalDateTime.now())
                    .notes("Online payment processed successfully at order placement.")
                    .build();
            paymentRepository.save(payment);
        }

        // Notify Farmer
        notificationService.createNotification(
                product.getFarmer(),
                "New Order Received!",
                "Buyer " + buyer.getFullName() + " ordered " + saved.getQuantity() + " " + product.getUnit() + " of " + product.getName() + " (#" + saved.getOrderNumber() + ").",
                "NEW_ORDER",
                saved.getId()
        );

        return mapToOrderResponse(saved);
    }

    @Transactional
    public OrderResponse cancelOrder(Long id, CancelOrderRequest request, String userEmail) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found with id: " + id));

        User user = authService.getUserByEmail(userEmail);
        boolean isBuyer = order.getBuyer().getId().equals(user.getId());
        boolean isFarmer = order.getFarmer().getId().equals(user.getId());
        boolean isAdmin = user.getRole() == Role.ADMIN;

        if (!isBuyer && !isFarmer && !isAdmin) {
            throw new AccessDeniedCustomException("You are not authorized to cancel this order.");
        }

        if (order.getStatus() == OrderStatus.DELIVERED) {
            throw new BusinessException("Delivered orders cannot be cancelled.");
        }
        if (order.getStatus() == OrderStatus.CANCELLED) {
            throw new BusinessException("Order is already cancelled.");
        }

        // Restock inventory
        Product product = order.getProduct();
        product.setQuantity(product.getQuantity().add(order.getQuantity()));
        if (product.getStatus() == ProductStatus.SOLD_OUT) {
            product.setStatus(ProductStatus.AVAILABLE);
        }
        productService.saveProduct(product);

        // Update status
        order.setStatus(OrderStatus.CANCELLED);
        String reason = request != null && request.getReason() != null ? request.getReason() : "Cancelled by " + user.getRole();
        order.setCancellationReason(reason);

        // Refund status if paid
        if ("PAID".equals(order.getPaymentStatus())) {
            order.setPaymentStatus("REFUNDED");
            // Also mark payment as refunded if found
            List<Payment> payments = paymentRepository.findByOrderOrderByCreatedAtDesc(order);
            for (Payment p : payments) {
                if (p.getStatus() == PaymentStatus.COMPLETED) {
                    p.setStatus(PaymentStatus.REFUNDED);
                    p.setRefundAmount(p.getAmount());
                    p.setRefundReason(reason);
                    p.setRefundAt(LocalDateTime.now());
                    paymentRepository.save(p);
                }
            }
        }

        // Cancel associated Delivery if exists
        deliveryRepository.findByOrder(order).ifPresent(d -> {
            if (d.getStatus() != DeliveryStatus.DELIVERED) {
                d.setStatus(DeliveryStatus.CANCELLED);
                deliveryRepository.save(d);
            }
        });

        // Cancel associated Pickup if exists
        pickupRepository.findByOrder(order).ifPresent(p -> {
            if (p.getStatus() != PickupStatus.COMPLETED) {
                p.setStatus(PickupStatus.CANCELLED);
                p.setCancellationReason(reason);
                pickupRepository.save(p);
            }
        });

        Order saved = orderRepository.save(order);

        // Notify other party
        User recipient = isBuyer ? order.getFarmer() : order.getBuyer();
        notificationService.createNotification(
                recipient,
                "Order Cancelled",
                "Order #" + (order.getOrderNumber() != null ? order.getOrderNumber() : order.getId()) + " has been cancelled. Reason: " + reason,
                "ORDER_CANCELLED",
                saved.getId()
        );

        return mapToOrderResponse(saved);
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

        // If status changed to READY_FOR_PICKUP and fulfillment is delivery, ensure delivery is created/updated
        if (newStatus == OrderStatus.READY_FOR_PICKUP) {
            if ("DELIVERY".equalsIgnoreCase(saved.getFulfillmentType())) {
                deliveryRepository.findByOrder(saved).orElseGet(() -> {
                    Delivery delivery = Delivery.builder()
                            .order(saved)
                            .pickupAddress(saved.getPickupAddress() != null ? saved.getPickupAddress() : saved.getProduct().getLocation())
                            .deliveryAddress(saved.getDeliveryAddress() != null ? saved.getDeliveryAddress() : "Standard Delivery Address")
                            .status(DeliveryStatus.AVAILABLE)
                            .recipientPhone(saved.getBuyer().getPhone())
                            .build();
                    return deliveryRepository.save(delivery);
                });
            } else {
                // If Pickup, mark pickup appointment ready if exists
                pickupRepository.findByOrder(saved).ifPresent(p -> {
                    p.setStatus(PickupStatus.READY_FOR_PICKUP);
                    pickupRepository.save(p);
                });
            }

            // Notify buyer
            notificationService.createNotification(
                    saved.getBuyer(),
                    "Order Ready!",
                    "Your order #" + (saved.getOrderNumber() != null ? saved.getOrderNumber() : saved.getId()) + " is packaged and ready for pickup/delivery!",
                    "ORDER_READY",
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
                .orderNumber(order.getOrderNumber() != null ? order.getOrderNumber() : "ORD-" + order.getId())
                .bidId(order.getBid() != null ? order.getBid().getId() : null)
                .buyer(authService.mapToUserResponse(order.getBuyer()))
                .farmer(authService.mapToUserResponse(order.getFarmer()))
                .product(productService.mapToResponse(order.getProduct()))
                .quantity(order.getQuantity())
                .unitPrice(order.getUnitPrice())
                .totalAmount(order.getTotalAmount())
                .status(order.getStatus())
                .fulfillmentType(order.getFulfillmentType() != null ? order.getFulfillmentType() : "DELIVERY")
                .paymentStatus(order.getPaymentStatus() != null ? order.getPaymentStatus() : "PAID")
                .paymentMethod(order.getPaymentMethod() != null ? order.getPaymentMethod() : "CARD")
                .deliveryAddress(order.getDeliveryAddress())
                .pickupAddress(order.getPickupAddress())
                .cancellationReason(order.getCancellationReason())
                .notes(order.getNotes())
                .createdAt(order.getCreatedAt())
                .updatedAt(order.getUpdatedAt())
                .build();
    }
}
