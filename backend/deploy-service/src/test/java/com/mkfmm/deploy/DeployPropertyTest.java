package com.mkfmm.deploy;

import com.mkfmm.deploy.domain.model.Deployment;
import net.jqwik.api.*;
import net.jqwik.api.constraints.*;

import static org.assertj.core.api.Assertions.assertThat;

class DeployPropertyTest {

    @Property
    void deployVersionFormatIsConsistent(@ForAll @IntRange(min = 1, max = 1000) int iterations) {
        Deployment deployment = new Deployment("JSON", 100, 5000L, "/data/test", "admin");
        String version = deployment.getVersion();

        assertThat(version).matches("\\d{8}-\\d{6}");
    }

    @Property
    void nonAdminAlwaysDenied(@ForAll("nonAdminRoles") String role) {
        assertThat(role).isNotEqualTo("ADMIN");
        assertThat(role).isNotEqualTo("ROOT_ADMIN");
    }

    @Provide
    Arbitrary<String> nonAdminRoles() {
        return Arbitraries.of("READ", "WRITE", "GUEST", "", "user");
    }

    @Property
    void deploymentMetadataIsComplete(
            @ForAll @StringLength(min = 1, max = 50) String format,
            @ForAll @IntRange(min = 0, max = 100000) int totalRecords,
            @ForAll @LongRange(min = 0, max = 50_000_000) long fileSize) {

        Deployment deployment = new Deployment(format, totalRecords, fileSize, "/data/test/file", "admin01");

        assertThat(deployment.getVersion()).isNotBlank();
        assertThat(deployment.getFormat()).isEqualTo(format);
        assertThat(deployment.getTotalRecords()).isEqualTo(totalRecords);
        assertThat(deployment.getFileSize()).isEqualTo(fileSize);
        assertThat(deployment.getUserId()).isEqualTo("admin01");
        assertThat(deployment.getCreatedAt()).isNotNull();
    }
}
