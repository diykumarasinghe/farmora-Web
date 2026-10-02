package com.farmora.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "buyer_addresses")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BuyerAddress {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "buyer_id", nullable = false)
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler", "password"})
    private User buyer;

    @NotBlank(message = "Address label is required (e.g. Home, Kitchen, Warehouse)")
    @Column(nullable = false, length = 100)
    private String label;

    @Column(length = 100)
    private String recipientName;

    @Column(length = 30)
    private String phone;

    @NotBlank(message = "Street address is required")
    @Column(nullable = false, length = 255)
    private String streetAddress;

    @Column(length = 100)
    private String city;

    @Column(length = 100)
    private String district;

    @Column(length = 100)
    private String province;

    @Column(length = 20)
    private String postalCode;

    @Builder.Default
    @Column(nullable = false)
    private Boolean isDefault = false;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;
}
