package com.mkfmm.resource.domain.service;

import com.mkfmm.resource.domain.model.Resource;
import com.mkfmm.resource.domain.model.SimilarityResult;
import org.springframework.stereotype.Component;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Stream;

@Component
public class LevenshteinCalculator {

    public int calculate(String s1, String s2) {
        if (s1 == null || s2 == null) return Integer.MAX_VALUE;

        String a = s1.toLowerCase();
        String b = s2.toLowerCase();
        int m = a.length();
        int n = b.length();

        int[][] dp = new int[m + 1][n + 1];

        for (int i = 0; i <= m; i++) dp[i][0] = i;
        for (int j = 0; j <= n; j++) dp[0][j] = j;

        for (int i = 1; i <= m; i++) {
            for (int j = 1; j <= n; j++) {
                int cost = (a.charAt(i - 1) == b.charAt(j - 1)) ? 0 : 1;
                dp[i][j] = Math.min(
                        Math.min(dp[i - 1][j] + 1, dp[i][j - 1] + 1),
                        dp[i - 1][j - 1] + cost
                );
            }
        }

        return dp[m][n];
    }

    public List<SimilarityResult> findSimilar(String inputKey, String inputContent,
                                               List<Resource> candidates,
                                               int threshold, int maxResults) {
        Stream<SimilarityResult> keyMatches = candidates.stream()
                .map(r -> new SimilarityResult(r, calculate(inputKey, r.getResourceKey()),
                        SimilarityResult.MatchType.SIMILAR_KEY))
                .filter(sr -> sr.distance() <= threshold && sr.distance() > 0);

        Stream<SimilarityResult> contentMatches = candidates.stream()
                .map(r -> new SimilarityResult(r, calculate(inputContent, r.getContent()),
                        SimilarityResult.MatchType.SIMILAR_CONTENT))
                .filter(sr -> sr.distance() <= threshold && sr.distance() > 0);

        return Stream.concat(keyMatches, contentMatches)
                .sorted(Comparator.comparingInt(SimilarityResult::distance))
                .limit(maxResults)
                .toList();
    }
}
