package com.farmora.service;

import com.farmora.dto.product.ProductRequest;
import com.farmora.dto.product.ProductResponse;
import com.farmora.exception.AccessDeniedCustomException;
import com.farmora.exception.ResourceNotFoundException;
import com.farmora.model.Product;
import com.farmora.model.ProductStatus;
import com.farmora.model.Role;
import com.farmora.model.User;
import com.farmora.repository.ProductRepository;
import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;
    private final AuthService authService;

    public Page<ProductResponse> getAllProducts(String search, String category, String location, ProductStatus status, Pageable pageable) {
        Specification<Product> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (status != null) {
                predicates.add(cb.equal(root.get("status"), status));
            } else {
                // By default for public browsing, only show AVAILABLE products
                predicates.add(cb.equal(root.get("status"), ProductStatus.AVAILABLE));
            }

            if (search != null && !search.trim().isEmpty()) {
                String searchPattern = "%" + search.trim().toLowerCase() + "%";
                Predicate nameMatch = cb.like(cb.lower(root.get("name")), searchPattern);
                Predicate descMatch = cb.like(cb.lower(root.get("description")), searchPattern);
                predicates.add(cb.or(nameMatch, descMatch));
            }

            if (category != null && !category.trim().isEmpty() && !category.equalsIgnoreCase("all")) {
                predicates.add(cb.equal(cb.lower(root.get("category")), category.trim().toLowerCase()));
            }

            if (location != null && !location.trim().isEmpty()) {
                predicates.add(cb.like(cb.lower(root.get("location")), "%" + location.trim().toLowerCase() + "%"));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };

        return productRepository.findAll(spec, pageable).map(this::mapToResponse);
    }

    public ProductResponse getProductById(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + id));
        return mapToResponse(product);
    }

    public List<ProductResponse> getProductsByFarmer(String farmerEmail) {
        User farmer = authService.getUserByEmail(farmerEmail);
        return productRepository.findByFarmerOrderByCreatedAtDesc(farmer).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public ProductResponse createProduct(ProductRequest request, String farmerEmail) {
        User farmer = authService.getUserByEmail(farmerEmail);
        if (farmer.getRole() != Role.FARMER) {
            throw new AccessDeniedCustomException("Only registered farmers can list agricultural products.");
        }

        Product product = Product.builder()
                .name(request.getName().trim())
                .description(request.getDescription())
                .category(request.getCategory().trim())
                .price(request.getPrice())
                .quantity(request.getQuantity())
                .unit(request.getUnit().trim())
                .location(request.getLocation() != null ? request.getLocation().trim() : farmer.getAddress())
                .harvestDate(request.getHarvestDate())
                .imageUrl(request.getImageUrl())
                .videoUrl(request.getVideoUrl())
                .expiryDate(request.getExpiryDate())
                .farmingMethod(request.getFarmingMethod() != null ? request.getFarmingMethod() : "Organic")
                .qualityGrade(request.getQualityGrade() != null ? request.getQualityGrade() : "Grade A")
                .packagingInfo(request.getPackagingInfo())
                .minOrderQuantity(request.getMinOrderQuantity() != null ? request.getMinOrderQuantity() : BigDecimal.ONE)
                .wholesalePrice(request.getWholesalePrice())
                .bulkOrderAvailable(request.getBulkOrderAvailable() != null ? request.getBulkOrderAvailable() : true)
                .status(ProductStatus.AVAILABLE)
                .farmer(farmer)
                .build();

        Product saved = productRepository.save(product);
        return mapToResponse(saved);
    }

    @Transactional
    public ProductResponse updateProduct(Long id, ProductRequest request, String currentUserEmail) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + id));

        User currentUser = authService.getUserByEmail(currentUserEmail);
        boolean isOwner = product.getFarmer().getId().equals(currentUser.getId());
        boolean isAdmin = currentUser.getRole() == Role.ADMIN;

        if (!isOwner && !isAdmin) {
            throw new AccessDeniedCustomException("You are not authorized to update this product.");
        }

        product.setName(request.getName().trim());
        product.setDescription(request.getDescription());
        product.setCategory(request.getCategory().trim());
        product.setPrice(request.getPrice());
        product.setQuantity(request.getQuantity());
        product.setUnit(request.getUnit().trim());
        product.setLocation(request.getLocation());
        product.setHarvestDate(request.getHarvestDate());
        if (request.getImageUrl() != null && !request.getImageUrl().trim().isEmpty()) {
            product.setImageUrl(request.getImageUrl().trim());
        }
        if (request.getVideoUrl() != null) {
            product.setVideoUrl(request.getVideoUrl().trim());
        }
        if (request.getExpiryDate() != null) {
            product.setExpiryDate(request.getExpiryDate());
        }
        if (request.getFarmingMethod() != null) {
            product.setFarmingMethod(request.getFarmingMethod().trim());
        }
        if (request.getQualityGrade() != null) {
            product.setQualityGrade(request.getQualityGrade().trim());
        }
        if (request.getPackagingInfo() != null) {
            product.setPackagingInfo(request.getPackagingInfo().trim());
        }
        if (request.getMinOrderQuantity() != null) {
            product.setMinOrderQuantity(request.getMinOrderQuantity());
        }
        if (request.getWholesalePrice() != null) {
            product.setWholesalePrice(request.getWholesalePrice());
        }
        if (request.getBulkOrderAvailable() != null) {
            product.setBulkOrderAvailable(request.getBulkOrderAvailable());
        }

        Product updated = productRepository.save(product);
        return mapToResponse(updated);
    }

    @Transactional
    public void deleteProduct(Long id, String currentUserEmail) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + id));

        User currentUser = authService.getUserByEmail(currentUserEmail);
        boolean isOwner = product.getFarmer().getId().equals(currentUser.getId());
        boolean isAdmin = currentUser.getRole() == Role.ADMIN;

        if (!isOwner && !isAdmin) {
            throw new AccessDeniedCustomException("You are not authorized to delete this product.");
        }

        productRepository.delete(product);
    }

    @Transactional
    public ProductResponse updateProductStatus(Long id, ProductStatus status, String currentUserEmail) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + id));

        User currentUser = authService.getUserByEmail(currentUserEmail);
        boolean isOwner = product.getFarmer().getId().equals(currentUser.getId());
        boolean isAdmin = currentUser.getRole() == Role.ADMIN;

        if (!isOwner && !isAdmin) {
            throw new AccessDeniedCustomException("You are not authorized to change this product's status.");
        }

        product.setStatus(status);
        return mapToResponse(productRepository.save(product));
    }

    public Product getProductEntity(Long id) {
        return productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + id));
    }

    public Product saveProduct(Product product) {
        return productRepository.save(product);
    }

    public ProductResponse mapToResponse(Product product) {
        return ProductResponse.builder()
                .id(product.getId())
                .name(product.getName())
                .description(product.getDescription())
                .category(product.getCategory())
                .price(product.getPrice())
                .quantity(product.getQuantity())
                .unit(product.getUnit())
                .location(product.getLocation())
                .harvestDate(product.getHarvestDate())
                .imageUrl(product.getImageUrl())
                .videoUrl(product.getVideoUrl())
                .expiryDate(product.getExpiryDate())
                .farmingMethod(product.getFarmingMethod())
                .qualityGrade(product.getQualityGrade())
                .packagingInfo(product.getPackagingInfo())
                .minOrderQuantity(product.getMinOrderQuantity())
                .wholesalePrice(product.getWholesalePrice())
                .bulkOrderAvailable(product.getBulkOrderAvailable())
                .status(product.getStatus())
                .farmer(authService.mapToUserResponse(product.getFarmer()))
                .createdAt(product.getCreatedAt())
                .updatedAt(product.getUpdatedAt())
                .build();
    }
}
