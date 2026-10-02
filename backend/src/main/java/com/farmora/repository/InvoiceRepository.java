package com.farmora.repository;

import com.farmora.model.Invoice;
import com.farmora.model.Order;
import com.farmora.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface InvoiceRepository extends JpaRepository<Invoice, Long> {
    List<Invoice> findByBuyerOrderByCreatedAtDesc(User buyer);
    List<Invoice> findByFarmerOrderByCreatedAtDesc(User farmer);
    Optional<Invoice> findByOrder(Order order);
    Optional<Invoice> findByInvoiceNumber(String invoiceNumber);
    List<Invoice> findAllByOrderByCreatedAtDesc();
}
