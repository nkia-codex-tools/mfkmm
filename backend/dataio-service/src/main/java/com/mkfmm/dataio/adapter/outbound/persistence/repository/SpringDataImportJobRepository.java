package com.mkfmm.dataio.adapter.outbound.persistence.repository;

import com.mkfmm.dataio.adapter.outbound.persistence.document.ImportJobDocument;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface SpringDataImportJobRepository extends MongoRepository<ImportJobDocument, String> {
}
