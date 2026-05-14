package com.resourcemanager.history.repository;

import com.resourcemanager.history.entity.ChangeHistory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.Instant;

@Repository
public interface ChangeHistoryRepository extends JpaRepository<ChangeHistory, Long> {

    Page<ChangeHistory> findAllByOrderByChangedAtDesc(Pageable pageable);

    Page<ChangeHistory> findByResourceTypeOrderByChangedAtDesc(String resourceType, Pageable pageable);

    Page<ChangeHistory> findByChangedByOrderByChangedAtDesc(Long changedBy, Pageable pageable);

    @Query("SELECT h FROM ChangeHistory h WHERE " +
            "(:resourceType IS NULL OR h.resourceType = :resourceType) AND " +
            "(:changedBy IS NULL OR h.changedBy = :changedBy) AND " +
            "(:from IS NULL OR h.changedAt >= :from) AND " +
            "(:to IS NULL OR h.changedAt <= :to) " +
            "ORDER BY h.changedAt DESC")
    Page<ChangeHistory> findFiltered(String resourceType, Long changedBy, Instant from, Instant to, Pageable pageable);
}
