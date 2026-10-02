package com.farmora.dto.auth;

import com.farmora.model.Role;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserResponse {
    private Long id;
    private String fullName;
    private String email;
    private String phone;
    private Role role;
    private String address;
    private String profileImageUrl;
    private com.farmora.model.BuyerType buyerType;
    private String businessName;
    private String businessRegNumber;
    private Boolean active;
    private LocalDateTime createdAt;
}
