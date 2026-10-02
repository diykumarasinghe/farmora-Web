package com.farmora.repository;

import com.farmora.model.Delivery;
import com.farmora.model.DeliveryStatus;
import com.farmora.model.Order;
import com.farmora.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DeliveryRepository extends JpaRepository<Delivery, Long> {
    List<Delivery> findByStatusOrderByCreatedAtDesc(DeliveryStatus status);
    List<Delivery> findByDeliveryPartnerOrderByCreatedAtDesc(User deliveryPartner);
    Optional<Delivery> findByOrder(Order order);
}
