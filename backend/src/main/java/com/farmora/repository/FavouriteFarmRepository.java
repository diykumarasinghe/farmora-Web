package com.farmora.repository;

import com.farmora.model.FavouriteFarm;
import com.farmora.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FavouriteFarmRepository extends JpaRepository<FavouriteFarm, Long> {

    List<FavouriteFarm> findByBuyerOrderByCreatedAtDesc(User buyer);

    Optional<FavouriteFarm> findByBuyerAndFarmer(User buyer, User farmer);

    boolean existsByBuyerAndFarmer(User buyer, User farmer);

    void deleteByBuyerAndFarmer(User buyer, User farmer);

    long countByFarmer(User farmer);
}
