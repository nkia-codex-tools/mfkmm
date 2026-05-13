package com.mkfmm.auth.adapter.outbound.persistence;

import com.mkfmm.auth.adapter.outbound.persistence.document.RefreshTokenDocument;
import com.mkfmm.auth.adapter.outbound.persistence.repository.SpringDataRefreshTokenRepository;
import com.mkfmm.auth.application.port.outbound.RefreshTokenRepository;
import com.mkfmm.auth.domain.model.RefreshToken;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class MongoRefreshTokenRepositoryAdapter implements RefreshTokenRepository {

    private final SpringDataRefreshTokenRepository springRepo;

    public MongoRefreshTokenRepositoryAdapter(SpringDataRefreshTokenRepository springRepo) {
        this.springRepo = springRepo;
    }

    @Override
    public RefreshToken save(RefreshToken token) {
        RefreshTokenDocument doc = toDocument(token);
        RefreshTokenDocument saved = springRepo.save(doc);
        token.setId(saved.getId());
        return token;
    }

    @Override
    public Optional<RefreshToken> findByToken(String token) {
        return springRepo.findByToken(token).map(this::toDomain);
    }

    @Override
    public List<RefreshToken> findByUserId(String userId) {
        return springRepo.findByUserId(userId).stream().map(this::toDomain).toList();
    }

    @Override
    public void revokeAllByUserId(String userId) {
        List<RefreshTokenDocument> tokens = springRepo.findByUserIdAndRevokedFalse(userId);
        tokens.forEach(t -> t.setRevoked(true));
        springRepo.saveAll(tokens);
    }

    private RefreshToken toDomain(RefreshTokenDocument doc) {
        RefreshToken token = new RefreshToken();
        token.setId(doc.getId());
        token.setUserId(doc.getUserId());
        token.setToken(doc.getToken());
        token.setJti(doc.getJti());
        token.setExpiresAt(doc.getExpiresAt());
        token.setCreatedAt(doc.getCreatedAt());
        token.setRevoked(doc.isRevoked());
        return token;
    }

    private RefreshTokenDocument toDocument(RefreshToken token) {
        RefreshTokenDocument doc = new RefreshTokenDocument();
        doc.setId(token.getId());
        doc.setUserId(token.getUserId());
        doc.setToken(token.getToken());
        doc.setJti(token.getJti());
        doc.setExpiresAt(token.getExpiresAt());
        doc.setCreatedAt(token.getCreatedAt());
        doc.setRevoked(token.isRevoked());
        return doc;
    }
}
