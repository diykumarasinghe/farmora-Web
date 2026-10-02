package com.farmora.repository;

import com.farmora.model.Bid;
import com.farmora.model.BidNegotiationHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BidNegotiationHistoryRepository extends JpaRepository<BidNegotiationHistory, Long> {
    List<BidNegotiationHistory> findByBidOrderByTimestampAsc(Bid bid);
}
