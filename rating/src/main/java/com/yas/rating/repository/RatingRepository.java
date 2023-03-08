package com.yas.rating.repository;

import com.yas.rating.model.Rating;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

@Repository
public interface RatingRepository extends JpaRepository<Rating, Long> {
    Page<Rating> findByProductId(Long id, Pageable pageable);

    @Query(value = "SELECT SUM(r.ratingStar), COUNT(r) FROM Rating r Where r.productId = :productId")
    List<Object[]> getTotalStarsAndTotalRatings(@Param("productId") long productId);
}