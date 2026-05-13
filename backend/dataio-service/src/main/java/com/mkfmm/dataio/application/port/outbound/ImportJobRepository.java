package com.mkfmm.dataio.application.port.outbound;

import com.mkfmm.dataio.domain.model.ImportJob;

public interface ImportJobRepository {
    ImportJob save(ImportJob job);
}
