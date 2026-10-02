package com.farmora.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "bid_negotiation_history")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BidNegotiationHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "bid_id", nullable = false)
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private Bid bid;

    @Column(nullable = false, length = 30)
    private String actorRole; // "BUYER" or "FARMER"

    @Column(nullable = false, length = 100)
    private String actorName;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal offeredAmount;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal offeredQuantity;

    @Column(length = 255)
    private String note;

    @Column(length = 30)
    private String action; // "BID_PLACED", "COUNTER_OFFERED", "ACCEPTED", "REJECTED", "WITHDRAWN", "EXTENDED"

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime timestamp;
}
