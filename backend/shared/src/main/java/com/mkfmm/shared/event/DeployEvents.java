package com.mkfmm.shared.event;

public final class DeployEvents {

    private DeployEvents() {}

    public static final String DEPLOY_COMPLETED = "deploy.completed";

    public record DeployCompletedPayload(String deploymentId, String version, String format, int totalRecords) {}
}
