package com.mkfmm.resource.domain.model;

public record DuplicateCheckResult(
        boolean isDuplicate,
        Resource existingResource,
        String duplicateField
) {
    public static DuplicateCheckResult noDuplicate() {
        return new DuplicateCheckResult(false, null, null);
    }

    public static DuplicateCheckResult duplicate(Resource existing, String field) {
        return new DuplicateCheckResult(true, existing, field);
    }
}
