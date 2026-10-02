package com.farmora.repository;

import com.farmora.model.FarmProfile;
import com.farmora.model.FarmingMethod;
import com.farmora.model.User;
import com.farmora.model.VerificationStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FarmProfileRepository extends JpaRepository<FarmProfile, Long> {

    Optional<FarmProfile> findByFarmer(User farmer);

    Optional<FarmProfile> findByFarmerId(Long farmerId);

    List<FarmProfile> findByVerificationStatus(VerificationStatus status);

    @Query("SELECT fp FROM FarmProfile fp WHERE " +
           "(:province IS NULL OR LOWER(fp.province) = LOWER(:province)) AND " +
           "(:district IS NULL OR LOWER(fp.district) = LOWER(:district)) AND " +
           "(:farmingMethod IS NULL OR fp.farmingMethod = :farmingMethod) AND " +
           "(:verificationStatus IS NULL OR fp.verificationStatus = :verificationStatus) " +
           "ORDER BY fp.updatedAt DESC")
    List<FarmProfile> filterFarms(
            @Param("province") String province,
            @Param("district") String district,
            @Param("farmingMethod") FarmingMethod farmingMethod,
            @Param("verificationStatus") VerificationStatus verificationStatus
    );
}
