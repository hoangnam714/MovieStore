package com.btlon.movie.store.repository;

import com.btlon.movie.store.entity.DiscountCode;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface DiscountCodeRepository extends JpaRepository<DiscountCode, Long> {
    Optional<DiscountCode> findByCodeIgnoreCase(String code);
    boolean existsByCodeIgnoreCase(String code);
    List<DiscountCode> findAllByOrderByIdDesc();

    /** Atomically consumes one use; returns 0 when the usage limit is already reached. */
    @Modifying(flushAutomatically = true)
    @Query("UPDATE DiscountCode d SET d.usedCount = d.usedCount + 1 "
            + "WHERE d.id = :id AND (d.usageLimit IS NULL OR d.usedCount < d.usageLimit)")
    int consumeOne(Long id);
}
