package com.farmora.dto.payment;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProcessPaymentRequest {
    @NotNull(message = "Order ID is required")
    private Long orderId;

    @NotNull(message = "Payment amount is required")
    @DecimalMin(value = "0.01", message = "Amount must be greater than 0")
    private BigDecimal amount;

    @NotNull(message = "Payment method is required")
    private String paymentMethod; // CARD, BANK_TRANSFER, CASH_ON_DELIVERY, WALLET

    // Optional card mock details
    private String cardNumber;
    private String cardExpiry;
    private String cardCvv;
    private String cardHolderName;

    // Optional bank transfer details
    private String bankReference;

    private String notes;
}
