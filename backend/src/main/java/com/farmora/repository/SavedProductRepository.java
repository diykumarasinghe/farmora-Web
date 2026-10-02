package com.farmora.repository;

import com.farmora.model.Product;
import com.farmora.model.SavedProduct;
import com.farmora.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SavedProductRepository extends JpaRepository<SavedProduct, Long> {
    List<SavedProduct> findByBuyerOrderByCreatedAtDesc(User buyer);
    Optional<SavedProduct> findByBuyerAndProduct(User buyer, Product product);
    void deleteByBuyerAndProduct(User buyer, Product product);
    boolean existsByBuyerAndProduct(User buyer, Product product);
}
