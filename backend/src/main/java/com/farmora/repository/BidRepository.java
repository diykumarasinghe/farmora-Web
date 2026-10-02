package com.farmora.repository;

import com.farmora.model.Bid;
import com.farmora.model.BidStatus;
import com.farmora.model.Product;
import com.farmora.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BidRepository extends JpaRepository<Bid, Long> {
    List<Bid> findByBuyerOrderByCreatedAtDesc(User buyer);
    List<Bid> findByProductOrderByCreatedAtDesc(Product product);
    List<Bid> findByProduct_FarmerOrderByCreatedAtDesc(User farmer);
    List<Bid> findByProductAndStatus(Product product, BidStatus status);
}
