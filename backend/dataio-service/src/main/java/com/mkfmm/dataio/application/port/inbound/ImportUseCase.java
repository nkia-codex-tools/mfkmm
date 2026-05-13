package com.mkfmm.dataio.application.port.inbound;

import com.mkfmm.dataio.domain.model.ConflictPolicy;
import com.mkfmm.dataio.domain.model.ImportJob;
import org.springframework.web.multipart.MultipartFile;

public interface ImportUseCase {
    ImportJob importFile(MultipartFile file, ConflictPolicy conflictPolicy, String userId);
}
