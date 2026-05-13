package com.mkfmm.deploy.application.service;

import com.mkfmm.deploy.application.port.inbound.DeployUseCase;
import com.mkfmm.deploy.application.port.outbound.DataioServicePort;
import com.mkfmm.deploy.application.port.outbound.DeploymentRepository;
import com.mkfmm.deploy.application.port.outbound.EventPublisherPort;
import com.mkfmm.deploy.domain.model.Deployment;
import com.mkfmm.shared.event.BaseEvent;
import com.mkfmm.shared.exception.BusinessException;
import com.mkfmm.shared.exception.ForbiddenException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;

@Service
public class DeployApplicationService implements DeployUseCase {

    private final DataioServicePort dataioService;
    private final DeploymentRepository deploymentRepository;
    private final EventPublisherPort eventPublisher;
    private final String deployFilePath;

    public DeployApplicationService(DataioServicePort dataioService,
                                    DeploymentRepository deploymentRepository,
                                    EventPublisherPort eventPublisher,
                                    @Value("${mkfmm.deploy.file-path}") String deployFilePath) {
        this.dataioService = dataioService;
        this.deploymentRepository = deploymentRepository;
        this.eventPublisher = eventPublisher;
        this.deployFilePath = deployFilePath;
    }

    @Override
    public Deployment deploy(String format, String userId, String userRole) {
        requireAdmin(userRole);

        byte[] fileData = dataioService.exportAll(format, userId);
        if (fileData == null || fileData.length == 0) {
            throw new BusinessException("DEPLOY_002", "EXPORT 실패: 데이터가 없습니다");
        }

        Deployment deployment = new Deployment(format, 0, fileData.length, "", userId);
        String version = deployment.getVersion();
        String extension = getExtension(format);
        String fileName = "export_all_" + version.replace("-", "_") + extension;

        Path dir = Paths.get(deployFilePath, version);
        Path filePath = dir.resolve(fileName);
        try {
            Files.createDirectories(dir);
            Files.write(filePath, fileData);
        } catch (IOException e) {
            throw new BusinessException("DEPLOY_002", "배포 파일 저장 실패: " + e.getMessage());
        }

        deployment.setFilePath(filePath.toString());
        deployment = deploymentRepository.save(deployment);

        publishDeployEvent(deployment, userId);
        return deployment;
    }

    @Override
    public Page<Deployment> getDeployHistory(Pageable pageable, String userRole) {
        requireAdmin(userRole);
        return deploymentRepository.findAll(pageable);
    }

    @Override
    public byte[] redownload(String deploymentId, String userRole) {
        requireAdmin(userRole);

        Deployment deployment = deploymentRepository.findById(deploymentId)
                .orElseThrow(() -> new BusinessException("DEPLOY_003", "배포를 찾을 수 없습니다"));

        Path filePath = Paths.get(deployment.getFilePath());
        if (!Files.exists(filePath)) {
            throw new BusinessException("DEPLOY_004", "배포 파일을 찾을 수 없습니다");
        }

        try {
            return Files.readAllBytes(filePath);
        } catch (IOException e) {
            throw new BusinessException("DEPLOY_004", "파일 읽기 실패: " + e.getMessage());
        }
    }

    private void requireAdmin(String userRole) {
        if (!"ADMIN".equals(userRole) && !"ROOT_ADMIN".equals(userRole)) {
            throw new ForbiddenException("배포는 관리자만 수행할 수 있습니다");
        }
    }

    private String getExtension(String format) {
        return switch (format.toUpperCase()) {
            case "EXCEL" -> ".xlsx";
            case "TSV" -> ".tsv";
            case "JSON" -> ".json";
            default -> ".bin";
        };
    }

    private void publishDeployEvent(Deployment deployment, String userId) {
        Map<String, Object> payload = Map.of(
            "deploymentId", deployment.getId(),
            "version", deployment.getVersion(),
            "format", deployment.getFormat(),
            "totalRecords", deployment.getTotalRecords()
        );
        eventPublisher.publish("deploy.completed", BaseEvent.of("deploy.completed", userId, payload));
    }
}
