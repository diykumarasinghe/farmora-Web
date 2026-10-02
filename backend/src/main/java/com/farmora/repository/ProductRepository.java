package com.farmora.repository;

import com.farmora.model.Product;
import com.farmora.model.ProductStatus;
import com.farmora.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long>, JpaSpecificationExecutor<Product> {
    List<Product> findByFarmerOrderByCreatedAtDesc(User farmer);
    List<Product> findByStatusOrderByCreatedAtDesc(ProductStatus status);
}
