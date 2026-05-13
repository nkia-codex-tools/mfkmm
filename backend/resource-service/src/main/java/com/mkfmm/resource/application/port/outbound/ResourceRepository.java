package com.mkfmm.resource.application.port.outbound;

import com.mkfmm.resource.domain.model.Resource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

public interface ResourceRepository {
    Resource save(Resource resource);
    Optional<Resource> findById(String id);
    Optional<Resource> findByResourceKeyAndNotDeleted(String resourceKey);
    boolean existsByResourceKeyAndNotDeleted(String resourceKey);
    Page<Resource> search(String keyword, String resourceType, String createdBy,
                          Instant startDate, Instant endDate, Pageable pageable);
    List<Resource> findCandidatesForSimilarity(String keyword);
    long hardDeleteByDeletedAtBefore(Instant cutoff);
    void deleteById(String id);
}
