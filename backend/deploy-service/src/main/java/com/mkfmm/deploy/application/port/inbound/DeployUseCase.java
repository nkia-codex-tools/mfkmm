package com.mkfmm.deploy.application.port.inbound;

import com.mkfmm.deploy.domain.model.Deployment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface DeployUseCase {
    Deployment deploy(String format, String userId, String userRole);
    Page<Deployment> getDeployHistory(Pageable pageable, String userRole);
    byte[] redownload(String deploymentId, String userRole);
}
