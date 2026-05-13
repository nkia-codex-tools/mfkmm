package com.mkfmm.resource;

import com.mkfmm.resource.domain.service.LevenshteinCalculator;
import net.jqwik.api.*;
import net.jqwik.api.constraints.StringLength;

import static org.junit.jupiter.api.Assertions.*;

class LevenshteinPropertyTest {

    private final LevenshteinCalculator calculator = new LevenshteinCalculator();

    @Property
    void symmetry(@ForAll @StringLength(min = 1, max = 20) String a,
                  @ForAll @StringLength(min = 1, max = 20) String b) {
        assertEquals(calculator.calculate(a, b), calculator.calculate(b, a),
                "Levenshtein distance must be symmetric");
    }

    @Property
    void identity(@ForAll @StringLength(min = 1, max = 20) String a) {
        assertEquals(0, calculator.calculate(a, a),
                "Distance of a string to itself must be 0");
    }

    @Property
    void triangleInequality(@ForAll @StringLength(min = 1, max = 10) String a,
                            @ForAll @StringLength(min = 1, max = 10) String b,
                            @ForAll @StringLength(min = 1, max = 10) String c) {
        int ab = calculator.calculate(a, b);
        int bc = calculator.calculate(b, c);
        int ac = calculator.calculate(a, c);
        assertTrue(ac <= ab + bc,
                "Triangle inequality must hold: d(a,c) <= d(a,b) + d(b,c)");
    }

    @Property
    void nonNegative(@ForAll @StringLength(min = 1, max = 20) String a,
                     @ForAll @StringLength(min = 1, max = 20) String b) {
        assertTrue(calculator.calculate(a, b) >= 0,
                "Distance must be non-negative");
    }

    @Property
    void upperBound(@ForAll @StringLength(min = 1, max = 20) String a,
                    @ForAll @StringLength(min = 1, max = 20) String b) {
        int distance = calculator.calculate(a, b);
        int maxLen = Math.max(a.length(), b.length());
        assertTrue(distance <= maxLen,
                "Distance cannot exceed the length of the longer string");
    }

    @Property
    void oracleComparison(@ForAll @StringLength(min = 1, max = 8) String a,
                          @ForAll @StringLength(min = 1, max = 8) String b) {
        int optimized = calculator.calculate(a, b);
        int bruteForce = bruteForceLevenshtein(a.toLowerCase(), b.toLowerCase());
        assertEquals(bruteForce, optimized,
                "Optimized implementation must match brute force");
    }

    private int bruteForceLevenshtein(String s1, String s2) {
        if (s1.isEmpty()) return s2.length();
        if (s2.isEmpty()) return s1.length();

        int cost = (s1.charAt(0) == s2.charAt(0)) ? 0 : 1;

        return Math.min(Math.min(
                bruteForceLevenshtein(s1.substring(1), s2) + 1,
                bruteForceLevenshtein(s1, s2.substring(1)) + 1),
                bruteForceLevenshtein(s1.substring(1), s2.substring(1)) + cost);
    }
}
