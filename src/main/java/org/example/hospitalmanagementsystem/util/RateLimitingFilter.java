package org.example.hospitalmanagementsystem.util;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.BucketConfiguration;
import io.github.bucket4j.Refill;
import io.github.bucket4j.distributed.proxy.ProxyManager;
import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.Duration;
import java.util.Set;


@Component
public class RateLimitingFilter implements Filter {

    private static final int CAPACITY = 4;
    private static final Duration REFILL_PERIOD = Duration.ofMinutes(1);
    private static final Set<String> PROTECTED_PATHS = Set.of("/login");

    private final ProxyManager<String> proxyManager;

    public RateLimitingFilter(ProxyManager<String> proxyManager) {
        this.proxyManager = proxyManager;
    }

    private BucketConfiguration bucketConfiguration() {
        return BucketConfiguration.builder()
                .addLimit(Bandwidth.classic(CAPACITY, Refill.intervally(CAPACITY, REFILL_PERIOD)))
                .build();
    }


    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;
        String path = httpRequest.getRequestURI();

        if (!PROTECTED_PATHS.contains(path.toLowerCase())) {
            chain.doFilter(request, response);
            return;
        }

        String key = "rl:" + clientKey(httpRequest);
        Bucket bucket = proxyManager.builder().build(key, this::bucketConfiguration);

        if (bucket.tryConsume(1)) {
            chain.doFilter(request, response);
        } else {
            httpResponse.setStatus(429);
            httpResponse.setHeader("Retry-After", "60");
            httpResponse.setContentType("application/json");
            httpResponse.getWriter().write("{\"error\":\"Too many requests. Please try again later.\"}");
        }
    }

    private String clientKey(HttpServletRequest request) {
        String forwarded = request.getHeader("X-Forwarded-For");
        String ip = (forwarded != null && !forwarded.isBlank())
                ? forwarded.split(",")[0].trim()
                : request.getRemoteAddr();
        return ip + ":" + request.getRequestURI();
    }
}