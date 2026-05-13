package com.mkfmm.resource.application.port.inbound;

import com.mkfmm.resource.domain.model.Resource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ResourceSearchUseCase {
    Page<Resource> search(String keyword, String resourceType, String createdBy,
                          String startDate, String endDate, Pageable pageable, String requesterId);
}
