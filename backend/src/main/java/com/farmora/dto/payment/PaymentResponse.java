package com.farmora.dto.payment;

import com.farmora.dto.auth.UserResponse;
import com.farmora.dto.order.OrderResponse;
import com.farmora.model.PaymentStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentResponse {
    private Long id;
    private String transactionReference;
    private OrderResponse order;
    private UserResponse buyer;
    private BigDecimal amount;
    private String paymentMethod;
    private PaymentStatus status;
    private String cardLast4;
    private String cardBrand;
    private String bankReference;
    private LocalDateTime paidAt;
    private BigDecimal refundAmount;
    private String refundReason;
    private LocalDateTime refundAt;
    private String notes;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
