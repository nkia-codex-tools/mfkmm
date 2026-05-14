package com.resourcemanager.resource.dto;

import jakarta.validation.constraints.Size;

public record FunctionRequest(
        @Size(max = 255) String aClass,
        @Size(max = 255) String bClass,
        @Size(max = 255) String cClass,
        @Size(max = 100) String action,
        @Size(max = 255) String functionName,
        @Size(max = 255) String functionId,
        @Size(max = 50) String type,
        Boolean light,
        Boolean standard,
        Boolean enterprise,
        Boolean systemMenu,
        @Size(max = 255) String productDomain,
        @Size(max = 255) String domainLicenseResourceType,
        @Size(max = 500) String relatedServices
) {}
