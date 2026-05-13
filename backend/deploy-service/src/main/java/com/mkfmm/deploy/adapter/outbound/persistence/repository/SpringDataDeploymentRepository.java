package com.mkfmm.deploy.adapter.outbound.persistence.repository;

import com.mkfmm.deploy.adapter.outbound.persistence.document.DeploymentDocument;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface SpringDataDeploymentRepository extends MongoRepository<DeploymentDocument, String> {
}
