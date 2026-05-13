package com.mkfmm.resource.application.port.inbound;

import com.mkfmm.resource.domain.model.Resource;

public interface ResourceUseCase {
    Object createResource(String resourceKey, String resourceType, String content,
                          String description, String requesterId, String requesterRole);
    Object confirmSimilarityChoice(String resourceKey, String resourceType, String content,
                                    String description, String chosenAction,
                                    String chosenResourceId, String reason,
                                    String requesterId, String requesterRole);
    Resource updateResource(String id, String content, String description,
                            String requesterId, String requesterRole);
    void deleteResource(String id, String requesterId, String requesterRole);
    Resource getResourceById(String id);
}
