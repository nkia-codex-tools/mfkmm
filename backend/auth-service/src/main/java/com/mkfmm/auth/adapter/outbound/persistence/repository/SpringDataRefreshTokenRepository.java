package com.mkfmm.auth.adapter.outbound.persistence.repository;

import com.mkfmm.auth.adapter.outbound.persistence.document.RefreshTokenDocument;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
import java.util.Optional;

public interface SpringDataRefreshTokenRepository extends MongoRepository<RefreshTokenDocument, String> {
    Optional<RefreshTokenDocument> findByToken(String token);
    List<RefreshTokenDocument> findByUserIdAndRevokedFalse(String userId);
    List<RefreshTokenDocument> findByUserId(String userId);
}
