package com.farmora.repository;

import com.farmora.model.Order;
import com.farmora.model.PickupAppointment;
import com.farmora.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PickupRepository extends JpaRepository<PickupAppointment, Long> {
    List<PickupAppointment> findByBuyerOrderByCreatedAtDesc(User buyer);
    List<PickupAppointment> findByFarmerOrderByCreatedAtDesc(User farmer);
    Optional<PickupAppointment> findByOrder(Order order);
}
