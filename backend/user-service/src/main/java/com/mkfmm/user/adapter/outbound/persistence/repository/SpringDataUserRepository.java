package com.mkfmm.user.adapter.outbound.persistence.repository;

import com.mkfmm.user.adapter.outbound.persistence.document.UserDocument;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

public interface SpringDataUserRepository extends MongoRepository<UserDocument, String> {
    Optional<UserDocument> findByUserId(String userId);
    boolean existsByUserId(String userId);
}
