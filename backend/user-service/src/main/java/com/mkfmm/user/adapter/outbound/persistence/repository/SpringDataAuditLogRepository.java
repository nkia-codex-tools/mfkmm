package com.mkfmm.user.adapter.outbound.persistence.repository;

import com.mkfmm.user.adapter.outbound.persistence.document.AuditLogDocument;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.time.Instant;

public interface SpringDataAuditLogRepository extends MongoRepository<AuditLogDocument, String> {
    long deleteByPerformedAtBefore(Instant cutoff);
}
