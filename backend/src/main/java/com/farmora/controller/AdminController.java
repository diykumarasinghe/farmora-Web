package com.farmora.controller;

import com.farmora.dto.auth.UserResponse;
import com.farmora.dto.complaint.ComplaintResponse;
import com.farmora.dto.farm.FarmProfileResponse;
import com.farmora.dto.order.OrderResponse;
import com.farmora.dto.payment.PaymentResponse;
import com.farmora.dto.product.ProductResponse;
import com.farmora.model.*;
import com.farmora.repository.BidRepository;
import com.farmora.repository.OrderRepository;
import com.farmora.repository.PaymentRepository;
import com.farmora.repository.ProductRepository;
import com.farmora.repository.UserRepository;
import com.farmora.service.*;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/admin")
@PreAuthorize("hasRole('ADMIN')")
@RequiredArgsConstructor
public class AdminController {

    private final UserRepository userRepository;
    private final ProductRepository productRepository;
    private final OrderRepository orderRepository;
    private final BidRepository bidRepository;
    private final PaymentRepository paymentRepository;
    private final AuthService authService;
    private final ProductService productService;
    private final OrderService orderService;
    private final PaymentService paymentService;
    private final FarmProfileService farmProfileService;
    private final ComplaintService complaintService;

    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> getAdminStats() {
        long totalUsers = userRepository.count();
        long totalProducts = productRepository.count();
        long totalBids = bidRepository.count();
        long totalOrders = orderRepository.count();

        BigDecimal totalRevenue = orderRepository.findAll().stream()
                .map(order -> order.getTotalAmount() != null ? order.getTotalAmount() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        Map<String, Object> stats = new HashMap<>();
        stats.put("totalUsers", totalUsers);
        stats.put("totalProducts", totalProducts);
        stats.put("totalBids", totalBids);
        stats.put("totalOrders", totalOrders);
        stats.put("totalRevenue", totalRevenue);

        return ResponseEntity.ok(stats);
    }

    @GetMapping("/users")
    public ResponseEntity<List<UserResponse>> getAllUsers() {
        List<UserResponse> users = userRepository.findAll().stream()
                .map(authService::mapToUserResponse)
                .collect(Collectors.toList());
        return ResponseEntity.ok(users);
    }

    @PutMapping("/users/{id}/toggle-status")
    public ResponseEntity<UserResponse> toggleUserStatus(@PathVariable Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found: " + id));
        user.setActive(!user.getActive());
        User saved = userRepository.save(user);
        return ResponseEntity.ok(authService.mapToUserResponse(saved));
    }

    @GetMapping("/products")
    public ResponseEntity<List<ProductResponse>> getAllAdminProducts() {
        List<ProductResponse> products = productRepository.findAll().stream()
                .map(productService::mapToResponse)
                .collect(Collectors.toList());
        return ResponseEntity.ok(products);
    }

    @PutMapping("/products/{id}/status")
    public ResponseEntity<ProductResponse> updateProductStatus(
            @PathVariable Long id,
            @RequestParam ProductStatus status
    ) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Product not found: " + id));
        product.setStatus(status);
        return ResponseEntity.ok(productService.mapToResponse(productRepository.save(product)));
    }

    @GetMapping("/orders")
    public ResponseEntity<List<OrderResponse>> getAllOrders() {
        List<OrderResponse> orders = orderRepository.findAllByOrderByCreatedAtDesc().stream()
                .map(orderService::mapToOrderResponse)
                .collect(Collectors.toList());
        return ResponseEntity.ok(orders);
    }

    // Farmer verification management
    @GetMapping("/verifications")
    public ResponseEntity<List<FarmProfileResponse>> getPendingVerifications() {
        return ResponseEntity.ok(farmProfileService.getFarms(null, null, null, null));
    }

    @PutMapping("/verifications/{id}/approve")
    public ResponseEntity<FarmProfileResponse> approveFarmerVerification(
            @PathVariable Long id,
            @RequestParam(required = false, defaultValue = "Profile approved by Platform Admin") String notes
    ) {
        return ResponseEntity.ok(farmProfileService.adminUpdateVerificationStatus(id, VerificationStatus.VERIFIED, notes));
    }

    @PutMapping("/verifications/{id}/reject")
    public ResponseEntity<FarmProfileResponse> rejectFarmerVerification(
            @PathVariable Long id,
            @RequestParam(required = false, defaultValue = "Documentation does not meet verification requirements.") String notes
    ) {
        return ResponseEntity.ok(farmProfileService.adminUpdateVerificationStatus(id, VerificationStatus.REJECTED, notes));
    }

    // Transactions monitoring
    @GetMapping("/transactions")
    public ResponseEntity<List<PaymentResponse>> getAllTransactions() {
        List<PaymentResponse> payments = paymentRepository.findAllByOrderByCreatedAtDesc().stream()
                .map(paymentService::mapToResponse)
                .collect(Collectors.toList());
        return ResponseEntity.ok(payments);
    }

    // Complaints / Reports management
    @GetMapping("/complaints")
    public ResponseEntity<List<ComplaintResponse>> getAllComplaints() {
        return ResponseEntity.ok(complaintService.getAllComplaints());
    }

    @PutMapping("/complaints/{id}/status")
    public ResponseEntity<ComplaintResponse> updateComplaintStatus(
            @PathVariable Long id,
            @RequestParam ComplaintStatus status,
            @RequestParam(required = false) String adminNotes
    ) {
        return ResponseEntity.ok(complaintService.updateComplaintStatus(id, status, adminNotes));
    }
}
