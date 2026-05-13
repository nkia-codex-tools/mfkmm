package com.mkfmm.auth.application.port.outbound;

import com.mkfmm.auth.domain.model.RefreshToken;

import java.util.List;
import java.util.Optional;

public interface RefreshTokenRepository {
    RefreshToken save(RefreshToken token);
    Optional<RefreshToken> findByToken(String token);
    List<RefreshToken> findByUserId(String userId);
    void revokeAllByUserId(String userId);
}
