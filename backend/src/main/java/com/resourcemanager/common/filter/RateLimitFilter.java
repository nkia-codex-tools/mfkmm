package com.resourcemanager.common.filter;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.Duration;
import java.util.concurrent.ConcurrentHashMap;

@Component
@Order(2)
public class RateLimitFilter extends OncePerRequestFilter {

    private final ConcurrentHashMap<String, Bucket> buckets = new ConcurrentHashMap<>();

    @Value("${rate-limit.login}")
    private int loginLimit;

    @Value("${rate-limit.register}")
    private int registerLimit;

    @Value("${rate-limit.api}")
    private int apiLimit;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        String key = resolveKey(request);
        int limit = resolveLimit(request);

        Bucket bucket = buckets.computeIfAbsent(key, k -> createBucket(limit));

        if (bucket.tryConsume(1)) {
            filterChain.doFilter(request, response);
        } else {
            response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
            response.setContentType("application/json");
            response.getWriter().write("{\"status\":429,\"error\":\"Too Many Requests\",\"message\":\"Rate limit exceeded. Please try again later.\"}");
        }
    }

    private String resolveKey(HttpServletRequest request) {
        String path = request.getRequestURI();
        String identifier = getClientIp(request);

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof Long userId) {
            identifier = String.valueOf(userId);
        }

        if (path.contains("/auth/login")) return "login:" + identifier;
        if (path.contains("/auth/register")) return "register:" + identifier;
        return "api:" + identifier;
    }

    private int resolveLimit(HttpServletRequest request) {
        String path = request.getRequestURI();
        if (path.contains("/auth/login")) return loginLimit;
        if (path.contains("/auth/register")) return registerLimit;
        return apiLimit;
    }

    private Bucket createBucket(int limit) {
        return Bucket.builder()
                .addLimit(Bandwidth.simple(limit, Duration.ofMinutes(1)))
                .build();
    }

    private String getClientIp(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
}
