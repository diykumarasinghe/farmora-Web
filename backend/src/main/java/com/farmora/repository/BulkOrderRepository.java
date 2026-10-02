package com.farmora.repository;

import com.farmora.model.BulkOrder;
import com.farmora.model.BulkOrderStatus;
import com.farmora.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BulkOrderRepository extends JpaRepository<BulkOrder, Long> {
    List<BulkOrder> findByBuyerOrderByCreatedAtDesc(User buyer);
    List<BulkOrder> findByFarmerOrderByCreatedAtDesc(User farmer);
    List<BulkOrder> findByStatusOrderByCreatedAtDesc(BulkOrderStatus status);
    List<BulkOrder> findAllByOrderByCreatedAtDesc();
}
