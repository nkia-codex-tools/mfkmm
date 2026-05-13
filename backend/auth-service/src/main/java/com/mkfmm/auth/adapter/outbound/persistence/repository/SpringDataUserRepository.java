package com.mkfmm.auth.adapter.outbound.persistence.repository;

import com.mkfmm.auth.adapter.outbound.persistence.document.UserDocument;
import com.mkfmm.auth.domain.model.Role;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

public interface SpringDataUserRepository extends MongoRepository<UserDocument, String> {
    Optional<UserDocument> findByUserId(String userId);
    boolean existsByRole(Role role);
}
