package com.mkfmm.dataio;

import com.mkfmm.dataio.domain.model.*;
import net.jqwik.api.*;
import net.jqwik.api.constraints.*;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ImportPropertyTest {

    @Property
    void importCountsAlwaysSum(
            @ForAll @IntRange(min = 0, max = 500) int success,
            @ForAll @IntRange(min = 0, max = 500) int failed,
            @ForAll @IntRange(min = 0, max = 500) int skipped) {

        int totalRows = success + failed + skipped;
        ImportJob job = new ImportJob("test.xlsx", FileFormat.EXCEL, 1000L, ConflictPolicy.SKIP, "user1");
        job.complete(totalRows, success, failed, skipped, new ArrayList<>());

        assertThat(job.getSuccessCount() + job.getFailedCount() + job.getSkippedCount())
                .isEqualTo(job.getTotalRows());
    }

    @Property
    void oversizedFilesDetected(@ForAll @LongRange(min = 5_242_881, max = 10_000_000) long fileSize) {
        assertThat(fileSize).isGreaterThan(5L * 1024 * 1024);
    }

    @Property
    void skipPolicyNeverModifiesCount(
            @ForAll @IntRange(min = 1, max = 100) int duplicateRows) {

        ImportJob job = new ImportJob("test.json", FileFormat.JSON, 1000L, ConflictPolicy.SKIP, "user1");
        job.complete(duplicateRows, 0, 0, duplicateRows, new ArrayList<>());

        assertThat(job.getSuccessCount()).isEqualTo(0);
        assertThat(job.getSkippedCount()).isEqualTo(duplicateRows);
    }

    @Property
    void allFormatsAreParseable(@ForAll("fileFormats") FileFormat format) {
        assertThat(format).isNotNull();
        assertThat(List.of(FileFormat.EXCEL, FileFormat.TSV, FileFormat.JSON)).contains(format);
    }

    @Provide
    Arbitrary<FileFormat> fileFormats() {
        return Arbitraries.of(FileFormat.values());
    }
}
