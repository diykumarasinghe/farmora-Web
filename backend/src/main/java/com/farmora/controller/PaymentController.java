package com.farmora.controller;

import com.farmora.dto.payment.PaymentResponse;
import com.farmora.dto.payment.ProcessPaymentRequest;
import com.farmora.dto.payment.RefundRequest;
import com.farmora.service.PaymentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;

    @PostMapping("/process")
    public ResponseEntity<PaymentResponse> processPayment(
            @Valid @RequestBody ProcessPaymentRequest request,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(paymentService.processPayment(request, userDetails.getUsername()));
    }

    @GetMapping("/my")
    public ResponseEntity<List<PaymentResponse>> getMyPayments(@AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(paymentService.getMyPayments(userDetails.getUsername()));
    }

    @GetMapping("/order/{orderId}")
    public ResponseEntity<List<PaymentResponse>> getPaymentsByOrderId(
            @PathVariable Long orderId,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        return ResponseEntity.ok(paymentService.getPaymentsByOrderId(orderId, userDetails.getUsername()));
    }

    @PostMapping("/{id}/refund")
    public ResponseEntity<PaymentResponse> handleRefund(
            @PathVariable Long id,
            @Valid @RequestBody RefundRequest request,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        return ResponseEntity.ok(paymentService.handleRefund(id, request, userDetails.getUsername()));
    }

    @PostMapping("/{id}/cancel")
    public ResponseEntity<PaymentResponse> cancelTransaction(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        return ResponseEntity.ok(paymentService.cancelTransaction(id, userDetails.getUsername()));
    }
}
