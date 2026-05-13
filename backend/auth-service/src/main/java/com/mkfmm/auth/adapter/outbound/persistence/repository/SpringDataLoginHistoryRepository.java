package com.mkfmm.auth.adapter.outbound.persistence.repository;

import com.mkfmm.auth.adapter.outbound.persistence.document.LoginHistoryDocument;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface SpringDataLoginHistoryRepository extends MongoRepository<LoginHistoryDocument, String> {
}
