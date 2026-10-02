package com.farmora.repository;

import com.farmora.model.Order;
import com.farmora.model.OrderStatus;
import com.farmora.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {
    List<Order> findByBuyerOrderByCreatedAtDesc(User buyer);
    List<Order> findByFarmerOrderByCreatedAtDesc(User farmer);
    List<Order> findByStatus(OrderStatus status);
    List<Order> findAllByOrderByCreatedAtDesc();
}
