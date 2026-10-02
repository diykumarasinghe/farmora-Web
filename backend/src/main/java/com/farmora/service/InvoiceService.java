package com.farmora.service;

import com.farmora.dto.invoice.InvoiceResponse;
import com.farmora.dto.invoice.UpdateInvoiceRequest;
import com.farmora.exception.AccessDeniedCustomException;
import com.farmora.exception.ResourceNotFoundException;
import com.farmora.model.*;
import com.farmora.repository.InvoiceRepository;
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
public class InvoiceService {

    private final InvoiceRepository invoiceRepository;
    private final OrderRepository orderRepository;
    private final AuthService authService;
    private final OrderService orderService;

    @Transactional
    public InvoiceResponse generateInvoiceForOrder(Long orderId, String userEmail) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found with id: " + orderId));

        User user = authService.getUserByEmail(userEmail);
        boolean isBuyer = order.getBuyer().getId().equals(user.getId());
        boolean isFarmer = order.getFarmer().getId().equals(user.getId());
        boolean isAdmin = user.getRole() == Role.ADMIN;

        if (!isBuyer && !isFarmer && !isAdmin) {
            throw new AccessDeniedCustomException("You are not authorized to access invoices for this order.");
        }

        Invoice invoice = invoiceRepository.findByOrder(order).orElse(null);
        if (invoice == null) {
            String invNum = "INV-" + (order.getOrderNumber() != null ? order.getOrderNumber().replace("ORD-", "") : order.getId());
            BigDecimal deliveryFee = "DELIVERY".equalsIgnoreCase(order.getFulfillmentType()) ? new BigDecimal("350.00") : BigDecimal.ZERO;
            BigDecimal grandTotal = order.getTotalAmount().add(deliveryFee);

            invoice = Invoice.builder()
                    .invoiceNumber(invNum)
                    .order(order)
                    .buyer(order.getBuyer())
                    .farmer(order.getFarmer())
                    .issueDate(order.getCreatedAt() != null ? order.getCreatedAt() : LocalDateTime.now())
                    .dueDate(LocalDateTime.now().plusDays(7))
                    .subtotal(order.getTotalAmount())
                    .taxAmount(BigDecimal.ZERO)
                    .deliveryFee(deliveryFee)
                    .discountAmount(BigDecimal.ZERO)
                    .totalAmount(grandTotal)
                    .billingName(order.getBuyer().getFullName())
                    .billingAddress(order.getDeliveryAddress() != null ? order.getDeliveryAddress() : order.getBuyer().getAddress())
                    .taxId("TIN-" + (order.getBuyer().getId() + 10000))
                    .businessNotes("Standard Agricultural Trade Invoice - Farmora Platform")
                    .paymentStatus(order.getPaymentStatus() != null ? order.getPaymentStatus() : "PAID")
                    .isArchived(false)
                    .build();
            invoice = invoiceRepository.save(invoice);
        } else {
            // Keep payment status synced
            if (order.getPaymentStatus() != null && !order.getPaymentStatus().equalsIgnoreCase(invoice.getPaymentStatus())) {
                invoice.setPaymentStatus(order.getPaymentStatus());
                invoice = invoiceRepository.save(invoice);
            }
        }

        return mapToResponse(invoice);
    }

    public InvoiceResponse getInvoiceByOrderId(Long orderId, String userEmail) {
        return generateInvoiceForOrder(orderId, userEmail);
    }

    public List<InvoiceResponse> getMyInvoices(Boolean archived, String userEmail) {
        User user = authService.getUserByEmail(userEmail);
        List<Invoice> list;

        if (user.getRole() == Role.BUYER) {
            list = invoiceRepository.findByBuyerOrderByCreatedAtDesc(user);
        } else if (user.getRole() == Role.FARMER) {
            list = invoiceRepository.findByFarmerOrderByCreatedAtDesc(user);
        } else {
            list = invoiceRepository.findAllByOrderByCreatedAtDesc();
        }

        if (archived != null) {
            list = list.stream()
                    .filter(i -> Boolean.valueOf(i.getIsArchived()).equals(archived))
                    .collect(Collectors.toList());
        }

        return list.stream().map(this::mapToResponse).collect(Collectors.toList());
    }

    @Transactional
    public InvoiceResponse updateInvoiceDetails(Long id, UpdateInvoiceRequest request, String userEmail) {
        Invoice invoice = invoiceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Invoice not found with id: " + id));

        User user = authService.getUserByEmail(userEmail);
        boolean isBuyer = invoice.getBuyer().getId().equals(user.getId());
        boolean isAdmin = user.getRole() == Role.ADMIN;

        if (!isBuyer && !isAdmin) {
            throw new AccessDeniedCustomException("Only the buyer or admin can edit billing invoice details.");
        }

        if (request.getBillingName() != null) invoice.setBillingName(request.getBillingName());
        if (request.getBillingAddress() != null) invoice.setBillingAddress(request.getBillingAddress());
        if (request.getTaxId() != null) invoice.setTaxId(request.getTaxId());
        if (request.getBusinessNotes() != null) invoice.setBusinessNotes(request.getBusinessNotes());

        return mapToResponse(invoiceRepository.save(invoice));
    }

    @Transactional
    public InvoiceResponse toggleArchive(Long id, String userEmail) {
        Invoice invoice = invoiceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Invoice not found with id: " + id));

        User user = authService.getUserByEmail(userEmail);
        boolean isBuyer = invoice.getBuyer().getId().equals(user.getId());
        boolean isFarmer = invoice.getFarmer().getId().equals(user.getId());
        boolean isAdmin = user.getRole() == Role.ADMIN;

        if (!isBuyer && !isFarmer && !isAdmin) {
            throw new AccessDeniedCustomException("You are not authorized to archive this receipt.");
        }

        invoice.setIsArchived(!Boolean.TRUE.equals(invoice.getIsArchived()));
        return mapToResponse(invoiceRepository.save(invoice));
    }

    public InvoiceResponse mapToResponse(Invoice inv) {
        return InvoiceResponse.builder()
                .id(inv.getId())
                .invoiceNumber(inv.getInvoiceNumber())
                .order(orderService.mapToOrderResponse(inv.getOrder()))
                .buyer(authService.mapToUserResponse(inv.getBuyer()))
                .farmer(authService.mapToUserResponse(inv.getFarmer()))
                .issueDate(inv.getIssueDate())
                .dueDate(inv.getDueDate())
                .subtotal(inv.getSubtotal())
                .taxAmount(inv.getTaxAmount())
                .deliveryFee(inv.getDeliveryFee())
                .discountAmount(inv.getDiscountAmount())
                .totalAmount(inv.getTotalAmount())
                .billingName(inv.getBillingName())
                .billingAddress(inv.getBillingAddress())
                .taxId(inv.getTaxId())
                .businessNotes(inv.getBusinessNotes())
                .paymentStatus(inv.getPaymentStatus())
                .isArchived(Boolean.TRUE.equals(inv.getIsArchived()))
                .createdAt(inv.getCreatedAt())
                .updatedAt(inv.getUpdatedAt())
                .build();
    }
}
