package com.farmora.service;

import com.farmora.dto.bid.BidRequest;
import com.farmora.dto.bid.BidResponse;
import com.farmora.dto.order.OrderResponse;
import com.farmora.exception.AccessDeniedCustomException;
import com.farmora.exception.BusinessException;
import com.farmora.exception.ResourceNotFoundException;
import com.farmora.model.*;
import com.farmora.repository.BidRepository;
import com.farmora.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BidService {

    private final BidRepository bidRepository;
    private final ProductService productService;
    private final AuthService authService;
    private final OrderRepository orderRepository;
    private final NotificationService notificationService;

    public List<BidResponse> getMyBids(String buyerEmail) {
        User buyer = authService.getUserByEmail(buyerEmail);
        return bidRepository.findByBuyerOrderByCreatedAtDesc(buyer).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public List<BidResponse> getProductBids(Long productId, String userEmail) {
        Product product = productService.getProductEntity(productId);
        User user = authService.getUserByEmail(userEmail);

        // Only product farmer or admin can view bids for a product
        boolean isOwner = product.getFarmer().getId().equals(user.getId());
        boolean isAdmin = user.getRole() == Role.ADMIN;
        if (!isOwner && !isAdmin) {
            throw new AccessDeniedCustomException("You are not authorized to view bids for this product.");
        }

        return bidRepository.findByProductOrderByCreatedAtDesc(product).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public List<BidResponse> getFarmerReceivedBids(String farmerEmail) {
        User farmer = authService.getUserByEmail(farmerEmail);
        return bidRepository.findByProduct_FarmerOrderByCreatedAtDesc(farmer).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public BidResponse createBid(BidRequest request, String buyerEmail) {
        User buyer = authService.getUserByEmail(buyerEmail);
        if (buyer.getRole() != Role.BUYER) {
            throw new AccessDeniedCustomException("Only registered buyers can place bids.");
        }

        Product product = productService.getProductEntity(request.getProductId());
        if (product.getStatus() != ProductStatus.AVAILABLE) {
            throw new BusinessException("Cannot bid on an unavailable product.");
        }

        if (request.getQuantity().compareTo(product.getQuantity()) > 0) {
            throw new BusinessException("Bid quantity (" + request.getQuantity() + " " + product.getUnit() +
                    ") exceeds available stock (" + product.getQuantity() + " " + product.getUnit() + ").");
        }

        Bid bid = Bid.builder()
                .product(product)
                .buyer(buyer)
                .amount(request.getAmount())
                .quantity(request.getQuantity())
                .status(BidStatus.PENDING)
                .note(request.getNote())
                .build();

        Bid saved = bidRepository.save(bid);

        // Notify farmer
        notificationService.createNotification(
                product.getFarmer(),
                "New Bid Received",
                "Buyer " + buyer.getFullName() + " placed a bid for " + saved.getQuantity() + " " + product.getUnit() + " of " + product.getName() + " at $" + saved.getAmount() + " each.",
                "BID_RECEIVED",
                saved.getId()
        );

        return mapToResponse(saved);
    }

    @Transactional
    public OrderResponse acceptBid(Long bidId, String farmerEmail) {
        Bid bid = bidRepository.findById(bidId)
                .orElseThrow(() -> new ResourceNotFoundException("Bid not found with id: " + bidId));

        User farmer = authService.getUserByEmail(farmerEmail);
        boolean isOwner = bid.getProduct().getFarmer().getId().equals(farmer.getId());
        boolean isAdmin = farmer.getRole() == Role.ADMIN;

        if (!isOwner && !isAdmin) {
            throw new AccessDeniedCustomException("Only the product owner can accept this bid.");
        }

        if (bid.getStatus() != BidStatus.PENDING) {
            throw new BusinessException("Only PENDING bids can be accepted. Current status: " + bid.getStatus());
        }

        Product product = bid.getProduct();
        if (product.getStatus() != ProductStatus.AVAILABLE) {
            throw new BusinessException("Product is no longer available.");
        }

        if (bid.getQuantity().compareTo(product.getQuantity()) > 0) {
            throw new BusinessException("Not enough product inventory remaining to accept this bid.");
        }

        // Deduct quantity
        BigDecimal remainingQty = product.getQuantity().subtract(bid.getQuantity());
        product.setQuantity(remainingQty);
        if (remainingQty.compareTo(BigDecimal.ZERO) <= 0) {
            product.setStatus(ProductStatus.SOLD_OUT);
        }
        productService.saveProduct(product);

        // Update bid status
        bid.setStatus(BidStatus.ACCEPTED);
        bidRepository.save(bid);

        // Create Order
        BigDecimal totalAmount = bid.getAmount().multiply(bid.getQuantity());
        Order order = Order.builder()
                .bid(bid)
                .buyer(bid.getBuyer())
                .farmer(product.getFarmer())
                .product(product)
                .quantity(bid.getQuantity())
                .unitPrice(bid.getAmount())
                .totalAmount(totalAmount)
                .status(OrderStatus.CONFIRMED)
                .deliveryAddress(bid.getBuyer().getAddress() != null ? bid.getBuyer().getAddress() : "Standard Delivery Address")
                .pickupAddress(product.getLocation() != null ? product.getLocation() : farmer.getAddress())
                .build();

        Order savedOrder = orderRepository.save(order);

        // Notify Buyer
        notificationService.createNotification(
                bid.getBuyer(),
                "Bid Accepted!",
                "Farmer " + farmer.getFullName() + " accepted your bid on " + product.getName() + "! Order #" + savedOrder.getId() + " has been created.",
                "BID_ACCEPTED",
                savedOrder.getId()
        );

        return mapToOrderResponse(savedOrder);
    }

    @Transactional
    public BidResponse rejectBid(Long bidId, String farmerEmail) {
        Bid bid = bidRepository.findById(bidId)
                .orElseThrow(() -> new ResourceNotFoundException("Bid not found with id: " + bidId));

        User farmer = authService.getUserByEmail(farmerEmail);
        boolean isOwner = bid.getProduct().getFarmer().getId().equals(farmer.getId());
        boolean isAdmin = farmer.getRole() == Role.ADMIN;

        if (!isOwner && !isAdmin) {
            throw new AccessDeniedCustomException("Only the product owner can reject this bid.");
        }

        if (bid.getStatus() != BidStatus.PENDING) {
            throw new BusinessException("Only PENDING bids can be rejected.");
        }

        bid.setStatus(BidStatus.REJECTED);
        Bid saved = bidRepository.save(bid);

        // Notify Buyer
        notificationService.createNotification(
                bid.getBuyer(),
                "Bid Update",
                "Your bid on " + bid.getProduct().getName() + " was declined by the farmer.",
                "BID_REJECTED",
                saved.getId()
        );

        return mapToResponse(saved);
    }

    @Transactional
    public BidResponse cancelBid(Long bidId, String buyerEmail) {
        Bid bid = bidRepository.findById(bidId)
                .orElseThrow(() -> new ResourceNotFoundException("Bid not found with id: " + bidId));

        User buyer = authService.getUserByEmail(buyerEmail);
        boolean isOwner = bid.getBuyer().getId().equals(buyer.getId());
        boolean isAdmin = buyer.getRole() == Role.ADMIN;

        if (!isOwner && !isAdmin) {
            throw new AccessDeniedCustomException("You can only cancel your own bid.");
        }

        if (bid.getStatus() != BidStatus.PENDING) {
            throw new BusinessException("Only PENDING bids can be cancelled.");
        }

        bid.setStatus(BidStatus.CANCELLED);
        return mapToResponse(bidRepository.save(bid));
    }

    public BidResponse mapToResponse(Bid bid) {
        BigDecimal totalOffer = bid.getAmount().multiply(bid.getQuantity());
        return BidResponse.builder()
                .id(bid.getId())
                .product(productService.mapToResponse(bid.getProduct()))
                .buyer(authService.mapToUserResponse(bid.getBuyer()))
                .amount(bid.getAmount())
                .quantity(bid.getQuantity())
                .totalOffer(totalOffer)
                .status(bid.getStatus())
                .note(bid.getNote())
                .createdAt(bid.getCreatedAt())
                .updatedAt(bid.getUpdatedAt())
                .build();
    }

    private OrderResponse mapToOrderResponse(Order order) {
        return OrderResponse.builder()
                .id(order.getId())
                .bidId(order.getBid() != null ? order.getBid().getId() : null)
                .buyer(authService.mapToUserResponse(order.getBuyer()))
                .farmer(authService.mapToUserResponse(order.getFarmer()))
                .product(productService.mapToResponse(order.getProduct()))
                .quantity(order.getQuantity())
                .unitPrice(order.getUnitPrice())
                .totalAmount(order.getTotalAmount())
                .status(order.getStatus())
                .deliveryAddress(order.getDeliveryAddress())
                .pickupAddress(order.getPickupAddress())
                .createdAt(order.getCreatedAt())
                .updatedAt(order.getUpdatedAt())
                .build();
    }
}
