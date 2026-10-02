package com.farmora.service;

import com.farmora.dto.payment.PaymentResponse;
import com.farmora.dto.payment.ProcessPaymentRequest;
import com.farmora.dto.payment.RefundRequest;
import com.farmora.exception.AccessDeniedCustomException;
import com.farmora.exception.BusinessException;
import com.farmora.exception.ResourceNotFoundException;
import com.farmora.model.*;
import com.farmora.repository.InvoiceRepository;
import com.farmora.repository.OrderRepository;
import com.farmora.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final OrderRepository orderRepository;
    private final InvoiceRepository invoiceRepository;
    private final AuthService authService;
    private final OrderService orderService;
    private final NotificationService notificationService;

    @Transactional
    public PaymentResponse processPayment(ProcessPaymentRequest request, String buyerEmail) {
        User buyer = authService.getUserByEmail(buyerEmail);
        Order order = orderRepository.findById(request.getOrderId())
                .orElseThrow(() -> new ResourceNotFoundException("Order not found with id: " + request.getOrderId()));

        if (!order.getBuyer().getId().equals(buyer.getId()) && buyer.getRole() != Role.ADMIN) {
            throw new AccessDeniedCustomException("You can only pay for your own orders.");
        }

        if (order.getStatus() == OrderStatus.CANCELLED) {
            throw new BusinessException("Cannot process payment for a cancelled order.");
        }

        String txnRef = "TXN-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        String method = request.getPaymentMethod() != null ? request.getPaymentMethod().toUpperCase() : "CARD";

        String last4 = "4242";
        String brand = "VISA";
        if (request.getCardNumber() != null && request.getCardNumber().replaceAll("\\s+", "").length() >= 4) {
            String sanitized = request.getCardNumber().replaceAll("\\s+", "");
            last4 = sanitized.substring(sanitized.length() - 4);
            if (sanitized.startsWith("5")) brand = "MASTERCARD";
            else if (sanitized.startsWith("3")) brand = "AMEX";
        }

        Payment payment = Payment.builder()
                .transactionReference(txnRef)
                .order(order)
                .buyer(buyer)
                .amount(request.getAmount())
                .paymentMethod(method)
                .status(PaymentStatus.COMPLETED)
                .cardLast4(method.equals("CARD") ? last4 : null)
                .cardBrand(method.equals("CARD") ? brand : null)
                .bankReference(request.getBankReference())
                .paidAt(LocalDateTime.now())
                .notes(request.getNotes() != null ? request.getNotes() : "Payment processed successfully via Farmora Gateway.")
                .build();

        Payment saved = paymentRepository.save(payment);

        // Update Order & Invoice payment status
        order.setPaymentStatus("PAID");
        order.setPaymentMethod(method);
        orderRepository.save(order);

        invoiceRepository.findByOrder(order).ifPresent(inv -> {
            inv.setPaymentStatus("PAID");
            invoiceRepository.save(inv);
        });

        // Notify Buyer
        notificationService.createNotification(
                buyer,
                "Payment Confirmed!",
                "Your payment of LKR " + saved.getAmount() + " for Order #" + (order.getOrderNumber() != null ? order.getOrderNumber() : order.getId()) + " has been processed (Ref: " + saved.getTransactionReference() + ").",
                "PAYMENT_SUCCESS",
                saved.getId()
        );

        return mapToResponse(saved);
    }

    public List<PaymentResponse> getMyPayments(String userEmail) {
        User user = authService.getUserByEmail(userEmail);
        if (user.getRole() == Role.BUYER) {
            return paymentRepository.findByBuyerOrderByCreatedAtDesc(user).stream()
                    .map(this::mapToResponse)
                    .collect(Collectors.toList());
        } else if (user.getRole() == Role.ADMIN) {
            return paymentRepository.findAllByOrderByCreatedAtDesc().stream()
                    .map(this::mapToResponse)
                    .collect(Collectors.toList());
        } else if (user.getRole() == Role.FARMER) {
            // Return payments for orders fulfilled by this farmer
            return paymentRepository.findAllByOrderByCreatedAtDesc().stream()
                    .filter(p -> p.getOrder().getFarmer().getId().equals(user.getId()))
                    .map(this::mapToResponse)
                    .collect(Collectors.toList());
        }
        return List.of();
    }

    public List<PaymentResponse> getPaymentsByOrderId(Long orderId, String userEmail) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found with id: " + orderId));

        return paymentRepository.findByOrderOrderByCreatedAtDesc(order).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public PaymentResponse handleRefund(Long paymentId, RefundRequest request, String userEmail) {
        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new ResourceNotFoundException("Payment transaction not found with id: " + paymentId));

        User user = authService.getUserByEmail(userEmail);
        boolean isBuyer = payment.getBuyer().getId().equals(user.getId());
        boolean isFarmer = payment.getOrder().getFarmer().getId().equals(user.getId());
        boolean isAdmin = user.getRole() == Role.ADMIN;

        if (!isBuyer && !isFarmer && !isAdmin) {
            throw new AccessDeniedCustomException("You are not authorized to process refund on this payment.");
        }

        if (payment.getStatus() == PaymentStatus.REFUNDED) {
            throw new BusinessException("Transaction has already been refunded.");
        }

        payment.setStatus(PaymentStatus.REFUNDED);
        payment.setRefundAmount(request.getAmount() != null ? request.getAmount() : payment.getAmount());
        payment.setRefundReason(request.getReason());
        payment.setRefundAt(LocalDateTime.now());

        Payment saved = paymentRepository.save(payment);

        // Update Order
        payment.getOrder().setPaymentStatus("REFUNDED");
        orderRepository.save(payment.getOrder());

        // Notify Buyer
        notificationService.createNotification(
                payment.getBuyer(),
                "Refund Processed",
                "A refund of LKR " + saved.getRefundAmount() + " for transaction " + saved.getTransactionReference() + " has been initiated. Reason: " + request.getReason(),
                "REFUND_PROCESSED",
                saved.getId()
        );

        return mapToResponse(saved);
    }

    @Transactional
    public PaymentResponse cancelTransaction(Long paymentId, String userEmail) {
        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new ResourceNotFoundException("Payment transaction not found with id: " + paymentId));

        User user = authService.getUserByEmail(userEmail);
        boolean isBuyer = payment.getBuyer().getId().equals(user.getId());
        boolean isAdmin = user.getRole() == Role.ADMIN;

        if (!isBuyer && !isAdmin) {
            throw new AccessDeniedCustomException("You are not authorized to cancel this transaction.");
        }

        if (payment.getStatus() == PaymentStatus.REFUNDED || payment.getStatus() == PaymentStatus.CANCELLED) {
            throw new BusinessException("Transaction is already " + payment.getStatus());
        }

        payment.setStatus(PaymentStatus.CANCELLED);
        payment.setNotes("Transaction cancelled by user.");
        Payment saved = paymentRepository.save(payment);

        payment.getOrder().setPaymentStatus("CANCELLED");
        orderRepository.save(payment.getOrder());

        return mapToResponse(saved);
    }

    public PaymentResponse mapToResponse(Payment p) {
        return PaymentResponse.builder()
                .id(p.getId())
                .transactionReference(p.getTransactionReference())
                .order(orderService.mapToOrderResponse(p.getOrder()))
                .buyer(authService.mapToUserResponse(p.getBuyer()))
                .amount(p.getAmount())
                .paymentMethod(p.getPaymentMethod())
                .status(p.getStatus())
                .cardLast4(p.getCardLast4())
                .cardBrand(p.getCardBrand())
                .bankReference(p.getBankReference())
                .paidAt(p.getPaidAt())
                .refundAmount(p.getRefundAmount())
                .refundReason(p.getRefundReason())
                .refundAt(p.getRefundAt())
                .notes(p.getNotes())
                .createdAt(p.getCreatedAt())
                .updatedAt(p.getUpdatedAt())
                .build();
    }
}
