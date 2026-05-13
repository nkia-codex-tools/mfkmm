package com.mkfmm.dataio.adapter.outbound.persistence;

import com.mkfmm.dataio.adapter.outbound.persistence.document.ImportJobDocument;
import com.mkfmm.dataio.adapter.outbound.persistence.repository.SpringDataImportJobRepository;
import com.mkfmm.dataio.application.port.outbound.ImportJobRepository;
import com.mkfmm.dataio.domain.model.ImportJob;
import org.springframework.stereotype.Component;

@Component
public class MongoImportJobRepositoryAdapter implements ImportJobRepository {

    private final SpringDataImportJobRepository repository;

    public MongoImportJobRepositoryAdapter(SpringDataImportJobRepository repository) {
        this.repository = repository;
    }

    @Override
    public ImportJob save(ImportJob job) {
        ImportJobDocument doc = ImportJobDocument.fromDomain(job);
        doc = repository.save(doc);
        job.setId(doc.getId());
        return job;
    }
}
