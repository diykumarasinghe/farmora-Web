package com.farmora.repository;

import com.farmora.model.BuyerAddress;
import com.farmora.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BuyerAddressRepository extends JpaRepository<BuyerAddress, Long> {

    List<BuyerAddress> findByBuyerOrderByIsDefaultDescCreatedAtDesc(User buyer);

    Optional<BuyerAddress> findByIdAndBuyer(Long id, User buyer);

    Optional<BuyerAddress> findByBuyerAndIsDefaultTrue(User buyer);
}
