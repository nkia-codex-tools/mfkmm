package com.resourcemanager.resource.dto;

import com.resourcemanager.resource.entity.FunctionResource;

public record FunctionResponse(
        Long id,
        String aClass, String bClass, String cClass,
        String action, String functionName, String functionId, String type,
        Boolean light, Boolean standard, Boolean enterprise, Boolean systemMenu,
        String productDomain, String domainLicenseResourceType, String relatedServices,
        String resourceKey,
        Integer rowOrder
) {
    public static FunctionResponse from(FunctionResource e) {
        String resourceKey = generateResourceKey(e.getFunctionId());
        return new FunctionResponse(
                e.getId(), e.getAClass(), e.getBClass(), e.getCClass(),
                e.getAction(), e.getFunctionName(), e.getFunctionId(), e.getType(),
                e.getLight(), e.getStandard(), e.getEnterprise(), e.getSystemMenu(),
                e.getProductDomain(), e.getDomainLicenseResourceType(), e.getRelatedServices(),
                resourceKey, e.getRowOrder()
        );
    }

    public static String generateResourceKey(String functionId) {
        if (functionId == null || functionId.isBlank()) return "";
        return "cmm.fn_" + functionId.toLowerCase().replace(".", "_");
    }
}
