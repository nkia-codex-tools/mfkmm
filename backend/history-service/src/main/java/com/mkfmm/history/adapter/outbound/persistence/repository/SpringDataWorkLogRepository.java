package com.mkfmm.history.adapter.outbound.persistence.repository;

import com.mkfmm.history.adapter.outbound.persistence.document.WorkLogDocument;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.time.Instant;

public interface SpringDataWorkLogRepository extends MongoRepository<WorkLogDocument, String> {
    boolean existsBySourceEvent(String sourceEvent);
    long deleteByMarkedForDeletionTrue();
    long deleteByWorkLogTypeAndPerformedAtBefore(String workLogType, Instant cutoff);
}
