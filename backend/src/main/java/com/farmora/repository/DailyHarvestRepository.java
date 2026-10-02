package com.farmora.repository;

import com.farmora.model.DailyHarvest;
import com.farmora.model.HarvestStatus;
import com.farmora.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface DailyHarvestRepository extends JpaRepository<DailyHarvest, Long> {
    List<DailyHarvest> findByFarmerOrderByCreatedAtDesc(User farmer);
    List<DailyHarvest> findByFarmerAndHarvestDateOrderByCreatedAtDesc(User farmer, LocalDate date);
    List<DailyHarvest> findByStatusOrderByCreatedAtDesc(HarvestStatus status);
    List<DailyHarvest> findByStatusAndQuantityGreaterThanOrderByCreatedAtDesc(HarvestStatus status, java.math.BigDecimal qty);
    List<DailyHarvest> findByExpiryDateBeforeAndStatusNot(LocalDate date, HarvestStatus status);
}
