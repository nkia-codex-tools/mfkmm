package com.mkfmm.resource;

import com.mkfmm.resource.domain.model.Resource;
import com.mkfmm.resource.domain.model.ResourceType;
import com.mkfmm.resource.domain.model.SimilarityResult;
import com.mkfmm.resource.domain.service.LevenshteinCalculator;
import net.jqwik.api.*;
import net.jqwik.api.constraints.AlphaChars;
import net.jqwik.api.constraints.StringLength;

import java.util.List;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.*;

class ResourceInvariantPropertyTest {

    private final LevenshteinCalculator calculator = new LevenshteinCalculator();

    @Property
    void similarityResults_neverExceedMaxResults(
            @ForAll @AlphaChars @StringLength(min = 3, max = 10) String inputKey,
            @ForAll @AlphaChars @StringLength(min = 3, max = 10) String inputContent) {

        List<Resource> candidates = IntStream.range(0, 20)
                .mapToObj(i -> new Resource(inputKey.substring(0, Math.min(inputKey.length(), 3)) + "x" + i,
                        ResourceType.MESSAGE_KEY, inputContent + i, null, "user1"))
                .toList();

        List<SimilarityResult> results = calculator.findSimilar(inputKey, inputContent, candidates, 3, 5);

        assertTrue(results.size() <= 5,
                "Similarity results must never exceed max 5");
    }

    @Property
    void similarityResults_allWithinThreshold(
            @ForAll @AlphaChars @StringLength(min = 3, max = 10) String inputKey,
            @ForAll @AlphaChars @StringLength(min = 3, max = 10) String inputContent) {

        List<Resource> candidates = IntStream.range(0, 10)
                .mapToObj(i -> new Resource(inputKey + (char)('a' + i),
                        ResourceType.FUNCTION_ID, inputContent + (char)('a' + i), null, "user1"))
                .toList();

        List<SimilarityResult> results = calculator.findSimilar(inputKey, inputContent, candidates, 3, 5);

        for (SimilarityResult result : results) {
            assertTrue(result.distance() <= 3,
                    "All results must have distance <= threshold (3), got: " + result.distance());
        }
    }

    @Property
    void similarityResults_sortedByDistance(
            @ForAll @AlphaChars @StringLength(min = 3, max = 8) String inputKey) {

        List<Resource> candidates = List.of(
                new Resource(inputKey + "xyz", ResourceType.MESSAGE_KEY, "content1", null, "user1"),
                new Resource(inputKey + "x", ResourceType.MESSAGE_KEY, "content2", null, "user1"),
                new Resource(inputKey + "ab", ResourceType.MESSAGE_KEY, "content3", null, "user1")
        );

        List<SimilarityResult> results = calculator.findSimilar(inputKey, "unrelated", candidates, 3, 5);

        for (int i = 1; i < results.size(); i++) {
            assertTrue(results.get(i).distance() >= results.get(i - 1).distance(),
                    "Results must be sorted by distance ascending");
        }
    }

    @Property
    void softDeletedResource_isNotActive() {
        Resource resource = new Resource("test.key", ResourceType.MESSAGE_KEY, "content", null, "user1");
        assertTrue(resource.isActive());

        resource.softDelete();
        assertFalse(resource.isActive());
        assertNotNull(resource.getDeletedAt());
    }
}
