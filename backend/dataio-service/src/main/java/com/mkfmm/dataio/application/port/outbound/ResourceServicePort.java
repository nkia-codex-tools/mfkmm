package com.mkfmm.dataio.application.port.outbound;

import java.util.List;
import java.util.Map;

public interface ResourceServicePort {
    boolean checkDuplicate(String key, String resourceType);
    String findExistingId(String key, String resourceType);
    void createResource(Map<String, String> resource, String userId);
    void updateResource(String id, Map<String, String> resource, String userId);
    List<Map<String, Object>> searchResources(Map<String, String> params);
    List<Map<String, Object>> findAllActive();
}
