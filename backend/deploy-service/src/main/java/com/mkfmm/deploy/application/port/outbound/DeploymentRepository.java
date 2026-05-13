package com.mkfmm.deploy.application.port.outbound;

import com.mkfmm.deploy.domain.model.Deployment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;

public interface DeploymentRepository {
    Deployment save(Deployment deployment);
    Page<Deployment> findAll(Pageable pageable);
    Optional<Deployment> findById(String id);
}
