package com.mkfmm.deploy.adapter.outbound.persistence;

import com.mkfmm.deploy.adapter.outbound.persistence.document.DeploymentDocument;
import com.mkfmm.deploy.adapter.outbound.persistence.repository.SpringDataDeploymentRepository;
import com.mkfmm.deploy.application.port.outbound.DeploymentRepository;
import com.mkfmm.deploy.domain.model.Deployment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class MongoDeploymentRepositoryAdapter implements DeploymentRepository {

    private final SpringDataDeploymentRepository repository;

    public MongoDeploymentRepositoryAdapter(SpringDataDeploymentRepository repository) {
        this.repository = repository;
    }

    @Override
    public Deployment save(Deployment deployment) {
        DeploymentDocument doc = DeploymentDocument.fromDomain(deployment);
        doc = repository.save(doc);
        deployment.setId(doc.getId());
        return deployment;
    }

    @Override
    public Page<Deployment> findAll(Pageable pageable) {
        return repository.findAll(pageable).map(DeploymentDocument::toDomain);
    }

    @Override
    public Optional<Deployment> findById(String id) {
        return repository.findById(id).map(DeploymentDocument::toDomain);
    }
}
