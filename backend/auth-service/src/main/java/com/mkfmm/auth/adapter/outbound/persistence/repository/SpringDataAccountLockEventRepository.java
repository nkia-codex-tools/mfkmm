package com.mkfmm.auth.adapter.outbound.persistence.repository;

import com.mkfmm.auth.adapter.outbound.persistence.document.AccountLockEventDocument;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

public interface SpringDataAccountLockEventRepository extends MongoRepository<AccountLockEventDocument, String> {
    Optional<AccountLockEventDocument> findFirstByUserIdOrderByLockedAtDesc(String userId);
}
