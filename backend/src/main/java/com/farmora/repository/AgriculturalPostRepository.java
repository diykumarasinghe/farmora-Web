package com.farmora.repository;

import com.farmora.model.AgriCategory;
import com.farmora.model.AgriculturalPost;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AgriculturalPostRepository extends JpaRepository<AgriculturalPost, Long> {
    List<AgriculturalPost> findAllByOrderByIsPinnedDescCreatedAtDesc();
    List<AgriculturalPost> findByCategoryOrderByIsPinnedDescCreatedAtDesc(AgriCategory category);
    List<AgriculturalPost> findByProvinceOrderByIsPinnedDescCreatedAtDesc(String province);
    List<AgriculturalPost> findByCategoryAndProvinceOrderByIsPinnedDescCreatedAtDesc(AgriCategory category, String province);
}
