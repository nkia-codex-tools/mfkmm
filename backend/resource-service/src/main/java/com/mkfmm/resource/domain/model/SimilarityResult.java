package com.mkfmm.resource.domain.model;

public record SimilarityResult(
        Resource resource,
        int distance,
        MatchType matchType
) {
    public enum MatchType {
        SIMILAR_KEY,
        SIMILAR_CONTENT
    }
}
