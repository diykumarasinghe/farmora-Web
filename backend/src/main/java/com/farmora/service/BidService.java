package com.farmora.service;

import com.farmora.dto.bid.BidRequest;
import com.farmora.dto.bid.BidResponse;
import com.farmora.dto.bid.CounterOfferRequest;
import com.farmora.dto.order.OrderResponse;
import com.farmora.exception.AccessDeniedCustomException;
import com.farmora.exception.BusinessException;
import com.farmora.exception.ResourceNotFoundException;
import com.farmora.model.*;
import com.farmora.dto.bid.BidHistoryResponse;
import com.farmora.repository.BidNegotiationHistoryRepository;
import com.farmora.repository.BidRepository;
import com.farmora.repository.OrderRepository;
import com.farmora.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BidService {

    private final BidRepository bidRepository;
    private final BidNegotiationHistoryRepository bidHistoryRepository;
    private final UserRepository userRepository;
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
                .expiresAt(LocalDateTime.now().plusHours(24))
                .archived(false)
                .build();

        Bid saved = bidRepository.save(bid);

        bidHistoryRepository.save(BidNegotiationHistory.builder()
                .bid(saved)
                .actorRole("BUYER")
                .actorName(buyer.getFullName())
                .offeredAmount(saved.getAmount())
                .offeredQuantity(saved.getQuantity())
                .note(saved.getNote())
                .action("BID_PLACED")
                .build());

        // Notify farmer
        notificationService.createNotification(
                product.getFarmer(),
                "New Bid Received",
                "Buyer " + buyer.getFullName() + " placed a bid for " + saved.getQuantity() + " " + product.getUnit() + " of " + product.getName() + " at LKR " + saved.getAmount() + " each.",
                "BID_RECEIVED",
                saved.getId()
        );

        return mapToResponse(saved);
    }

    @Transactional
    public BidResponse sendCounterOffer(Long bidId, CounterOfferRequest request, String userEmail) {
        Bid bid = bidRepository.findById(bidId)
                .orElseThrow(() -> new ResourceNotFoundException("Bid not found with id: " + bidId));

        User user = authService.getUserByEmail(userEmail);
        boolean isFarmer = bid.getProduct().getFarmer().getId().equals(user.getId());
        boolean isBuyer = bid.getBuyer().getId().equals(user.getId());
        boolean isAdmin = user.getRole() == Role.ADMIN;

        if (!isFarmer && !isBuyer && !isAdmin) {
            throw new AccessDeniedCustomException("You are not a participant in this bid negotiation.");
        }

        if (bid.getStatus() != BidStatus.PENDING && bid.getStatus() != BidStatus.COUNTERED) {
            throw new BusinessException("Cannot send a counter-offer on a bid with status: " + bid.getStatus());
        }

        bid.setCounterAmount(request.getCounterAmount());
        bid.setCounterNote(request.getCounterNote());
        bid.setStatus(BidStatus.COUNTERED);
        bid.setExpiresAt(LocalDateTime.now().plusHours(24));

        bidHistoryRepository.save(BidNegotiationHistory.builder()
                .bid(bid)
                .actorRole(isFarmer ? "FARMER" : "BUYER")
                .actorName(user.getFullName())
                .offeredAmount(request.getCounterAmount())
                .offeredQuantity(bid.getQuantity())
                .note(request.getCounterNote())
                .action("COUNTER_OFFERED")
                .build());

        if (isFarmer || isAdmin) {
            bid.setCounterBy("FARMER");
            bidRepository.save(bid);

            // Notify buyer
            notificationService.createNotification(
                    bid.getBuyer(),
                    "Counter-Offer Received",
                    "Farmer " + bid.getProduct().getFarmer().getFullName() + " sent a counter-offer of LKR " + request.getCounterAmount() + " per " + bid.getProduct().getUnit() + " for " + bid.getProduct().getName(),
                    "BID_COUNTERED",
                    bid.getId()
            );
        } else {
            bid.setCounterBy("BUYER");
            bidRepository.save(bid);

            // Notify farmer
            notificationService.createNotification(
                    bid.getProduct().getFarmer(),
                    "Counter-Offer Received",
                    "Buyer " + bid.getBuyer().getFullName() + " countered with LKR " + request.getCounterAmount() + " per " + bid.getProduct().getUnit() + " for " + bid.getProduct().getName(),
                    "BID_COUNTERED",
                    bid.getId()
            );
        }

        return mapToResponse(bid);
    }

    @Transactional
    public OrderResponse acceptCounterOffer(Long bidId, String userEmail) {
        Bid bid = bidRepository.findById(bidId)
                .orElseThrow(() -> new ResourceNotFoundException("Bid not found with id: " + bidId));

        User user = authService.getUserByEmail(userEmail);
        boolean isBuyer = bid.getBuyer().getId().equals(user.getId());
        boolean isFarmer = bid.getProduct().getFarmer().getId().equals(user.getId());
        boolean isAdmin = user.getRole() == Role.ADMIN;

        if (bid.getStatus() != BidStatus.COUNTERED) {
            throw new BusinessException("No active counter-offer exists to accept.");
        }

        // Check if the caller is the intended recipient of the counter offer
        if ("FARMER".equals(bid.getCounterBy()) && !isBuyer && !isAdmin) {
            throw new AccessDeniedCustomException("Only the buyer can accept a counter-offer proposed by the farmer.");
        }
        if ("BUYER".equals(bid.getCounterBy()) && !isFarmer && !isAdmin) {
            throw new AccessDeniedCustomException("Only the farmer can accept a counter-offer proposed by the buyer.");
        }

        // Apply counter price
        if (bid.getCounterAmount() != null) {
            bid.setAmount(bid.getCounterAmount());
        }

        return completeBidAcceptance(bid, user);
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

        if (bid.getStatus() != BidStatus.PENDING && bid.getStatus() != BidStatus.COUNTERED) {
            throw new BusinessException("Only PENDING or COUNTERED bids can be accepted. Current status: " + bid.getStatus());
        }

        return completeBidAcceptance(bid, farmer);
    }

    private OrderResponse completeBidAcceptance(Bid bid, User actor) {
        Product product = bid.getProduct();
        if (product.getStatus() != ProductStatus.AVAILABLE) {
            throw new BusinessException("Product is no longer available.");
        }

        if (bid.getQuantity().compareTo(product.getQuantity()) > 0) {
            throw new BusinessException("Not enough product inventory remaining to accept this bid.");
        }

        // Deduct inventory
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
        String orderNum = "ORD-" + (System.currentTimeMillis() % 1000000);

        Order order = Order.builder()
                .orderNumber(orderNum)
                .bid(bid)
                .buyer(bid.getBuyer())
                .farmer(product.getFarmer())
                .product(product)
                .quantity(bid.getQuantity())
                .unitPrice(bid.getAmount())
                .totalAmount(totalAmount)
                .status(OrderStatus.CONFIRMED)
                .fulfillmentType("DELIVERY")
                .paymentStatus("PAID")
                .paymentMethod("CARD")
                .deliveryAddress(bid.getBuyer().getAddress() != null ? bid.getBuyer().getAddress() : "Standard Delivery Address")
                .pickupAddress(product.getLocation() != null ? product.getLocation() : product.getFarmer().getAddress())
                .notes("Generated from negotiated bid #" + bid.getId() + (bid.getNote() != null ? ": " + bid.getNote() : ""))
                .build();

        Order savedOrder = orderRepository.save(order);

        // Notify Buyer
        notificationService.createNotification(
                bid.getBuyer(),
                "Bid Negotiation Accepted!",
                "Your offer on " + product.getName() + " has been accepted! Order #" + savedOrder.getOrderNumber() + " created.",
                "BID_ACCEPTED",
                savedOrder.getId()
        );

        return mapToOrderResponse(savedOrder);
    }

    @Transactional
    public BidResponse rejectBid(Long bidId, String userEmail) {
        Bid bid = bidRepository.findById(bidId)
                .orElseThrow(() -> new ResourceNotFoundException("Bid not found with id: " + bidId));

        User user = authService.getUserByEmail(userEmail);
        boolean isOwner = bid.getProduct().getFarmer().getId().equals(user.getId());
        boolean isBuyer = bid.getBuyer().getId().equals(user.getId());
        boolean isAdmin = user.getRole() == Role.ADMIN;

        if (!isOwner && !isBuyer && !isAdmin) {
            throw new AccessDeniedCustomException("You are not authorized to reject this bid.");
        }

        if (bid.getStatus() != BidStatus.PENDING && bid.getStatus() != BidStatus.COUNTERED) {
            throw new BusinessException("Only active bids can be declined.");
        }

        bid.setStatus(BidStatus.REJECTED);
        Bid saved = bidRepository.save(bid);

        User recipient = isOwner ? bid.getBuyer() : bid.getProduct().getFarmer();
        notificationService.createNotification(
                recipient,
                "Bid Offer Declined",
                "The bid offer on " + bid.getProduct().getName() + " was declined.",
                "BID_REJECTED",
                saved.getId()
        );

        return mapToResponse(saved);
    }

    @Transactional
    public BidResponse withdrawBid(Long bidId, String buyerEmail) {
        Bid bid = bidRepository.findById(bidId)
                .orElseThrow(() -> new ResourceNotFoundException("Bid not found with id: " + bidId));

        User buyer = authService.getUserByEmail(buyerEmail);
        boolean isOwner = bid.getBuyer().getId().equals(buyer.getId());
        boolean isAdmin = buyer.getRole() == Role.ADMIN;

        if (!isOwner && !isAdmin) {
            throw new AccessDeniedCustomException("You can only withdraw your own bid.");
        }

        if (bid.getStatus() != BidStatus.PENDING && bid.getStatus() != BidStatus.COUNTERED) {
            throw new BusinessException("Only active bids can be withdrawn.");
        }

        bid.setStatus(BidStatus.WITHDRAWN);
        Bid saved = bidRepository.save(bid);

        bidHistoryRepository.save(BidNegotiationHistory.builder()
                .bid(saved)
                .actorRole("BUYER")
                .actorName(buyer.getFullName())
                .offeredAmount(saved.getAmount())
                .offeredQuantity(saved.getQuantity())
                .note("Bid withdrawn by buyer")
                .action("WITHDRAWN")
                .build());

        return mapToResponse(saved);
    }

    @Transactional
    public BidResponse cancelBid(Long bidId, String buyerEmail) {
        return withdrawBid(bidId, buyerEmail);
    }

    public BidResponse getBidById(Long bidId, String username) {
        Bid bid = bidRepository.findById(bidId)
                .orElseThrow(() -> new ResourceNotFoundException("Bid not found with id: " + bidId));
        return mapToResponse(bid);
    }

    public List<BidResponse> getFarmerBidsById(Long farmerId) {
        User farmer = userRepository.findById(farmerId)
                .orElseThrow(() -> new ResourceNotFoundException("Farmer not found with id: " + farmerId));
        return bidRepository.findByProduct_FarmerOrderByCreatedAtDesc(farmer).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public List<BidResponse> getBuyerBidsById(Long buyerId) {
        User buyer = userRepository.findById(buyerId)
                .orElseThrow(() -> new ResourceNotFoundException("Buyer not found with id: " + buyerId));
        return bidRepository.findByBuyerOrderByCreatedAtDesc(buyer).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public List<BidHistoryResponse> getBidHistory(Long bidId, String username) {
        Bid bid = bidRepository.findById(bidId)
                .orElseThrow(() -> new ResourceNotFoundException("Bid not found with id: " + bidId));
        return bidHistoryRepository.findByBidOrderByTimestampAsc(bid).stream()
                .map(h -> BidHistoryResponse.builder()
                        .id(h.getId())
                        .bidId(bid.getId())
                        .actorRole(h.getActorRole())
                        .actorName(h.getActorName())
                        .offeredAmount(h.getOfferedAmount())
                        .offeredQuantity(h.getOfferedQuantity())
                        .note(h.getNote())
                        .action(h.getAction())
                        .timestamp(h.getTimestamp())
                        .build())
                .collect(Collectors.toList());
    }

    @Transactional
    public BidResponse extendBidExpiry(Long bidId, int extraHours, String username) {
        Bid bid = bidRepository.findById(bidId)
                .orElseThrow(() -> new ResourceNotFoundException("Bid not found with id: " + bidId));
        User user = authService.getUserByEmail(username);

        int hours = extraHours > 0 ? extraHours : 24;
        LocalDateTime baseTime = (bid.getExpiresAt() != null && bid.getExpiresAt().isAfter(LocalDateTime.now()))
                ? bid.getExpiresAt()
                : LocalDateTime.now();
        bid.setExpiresAt(baseTime.plusHours(hours));
        Bid saved = bidRepository.save(bid);

        bidHistoryRepository.save(BidNegotiationHistory.builder()
                .bid(saved)
                .actorRole(user.getRole().name())
                .actorName(user.getFullName())
                .offeredAmount(saved.getAmount())
                .offeredQuantity(saved.getQuantity())
                .note("Extended negotiation deadline by " + hours + " hours")
                .action("EXTENDED")
                .build());

        return mapToResponse(saved);
    }

    @Transactional
    public BidResponse archiveBid(Long bidId, String username) {
        Bid bid = bidRepository.findById(bidId)
                .orElseThrow(() -> new ResourceNotFoundException("Bid not found with id: " + bidId));
        bid.setArchived(true);
        return mapToResponse(bidRepository.save(bid));
    }

    public BidResponse mapToResponse(Bid bid) {
        BigDecimal totalOffer = bid.getAmount().multiply(bid.getQuantity());
        LocalDateTime expiresAt = bid.getExpiresAt();
        boolean isExpired = expiresAt != null && LocalDateTime.now().isAfter(expiresAt)
                && (bid.getStatus() == BidStatus.PENDING || bid.getStatus() == BidStatus.COUNTERED);

        return BidResponse.builder()
                .id(bid.getId())
                .product(productService.mapToResponse(bid.getProduct()))
                .buyer(authService.mapToUserResponse(bid.getBuyer()))
                .amount(bid.getAmount())
                .quantity(bid.getQuantity())
                .totalOffer(totalOffer)
                .status(bid.getStatus())
                .note(bid.getNote())
                .counterAmount(bid.getCounterAmount())
                .counterNote(bid.getCounterNote())
                .counterBy(bid.getCounterBy())
                .expiresAt(bid.getExpiresAt())
                .archived(Boolean.TRUE.equals(bid.getArchived()))
                .isExpired(isExpired)
                .createdAt(bid.getCreatedAt())
                .updatedAt(bid.getUpdatedAt())
                .build();
    }

    private OrderResponse mapToOrderResponse(Order order) {
        return OrderResponse.builder()
                .id(order.getId())
                .orderNumber(order.getOrderNumber() != null ? order.getOrderNumber() : "ORD-" + order.getId())
                .bidId(order.getBid() != null ? order.getBid().getId() : null)
                .buyer(authService.mapToUserResponse(order.getBuyer()))
                .farmer(authService.mapToUserResponse(order.getFarmer()))
                .product(productService.mapToResponse(order.getProduct()))
                .quantity(order.getQuantity())
                .unitPrice(order.getUnitPrice())
                .totalAmount(order.getTotalAmount())
                .status(order.getStatus())
                .fulfillmentType(order.getFulfillmentType() != null ? order.getFulfillmentType() : "DELIVERY")
                .paymentStatus(order.getPaymentStatus() != null ? order.getPaymentStatus() : "PAID")
                .paymentMethod(order.getPaymentMethod() != null ? order.getPaymentMethod() : "CARD")
                .deliveryAddress(order.getDeliveryAddress())
                .pickupAddress(order.getPickupAddress())
                .createdAt(order.getCreatedAt())
                .updatedAt(order.getUpdatedAt())
                .build();
    }
}
