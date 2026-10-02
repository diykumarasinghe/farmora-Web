package com.farmora.repository;

import com.farmora.model.Order;
import com.farmora.model.Payment;
import com.farmora.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long> {
    List<Payment> findByBuyerOrderByCreatedAtDesc(User buyer);
    List<Payment> findByOrderOrderByCreatedAtDesc(Order order);
    Optional<Payment> findByTransactionReference(String transactionReference);
    List<Payment> findAllByOrderByCreatedAtDesc();
}
