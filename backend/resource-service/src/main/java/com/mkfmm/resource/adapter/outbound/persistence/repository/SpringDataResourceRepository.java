package com.mkfmm.resource.adapter.outbound.persistence.repository;

import com.mkfmm.resource.adapter.outbound.persistence.document.ResourceDocument;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

public interface SpringDataResourceRepository extends MongoRepository<ResourceDocument, String> {
    Optional<ResourceDocument> findByResourceKeyAndDeletedFalse(String resourceKey);
    boolean existsByResourceKeyAndDeletedFalse(String resourceKey);
    List<ResourceDocument> findByDeletedFalse();
    long deleteByDeletedTrueAndDeletedAtBefore(Instant cutoff);
}
