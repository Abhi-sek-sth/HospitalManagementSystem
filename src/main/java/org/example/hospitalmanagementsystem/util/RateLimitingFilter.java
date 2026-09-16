package org.example.hospitalmanagementsystem.util;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.BucketConfiguration;
import io.github.bucket4j.Refill;
import io.github.bucket4j.distributed.proxy.ProxyManager;
import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.example.hospitalmanagementsystem.dto.request.RateLimitPolicy;
import org.springframework.stereotype.Component;
import java.io.IOException;




@Component
public class RateLimitingFilter implements Filter {


    private final RateLimitResolver rateLimitResolver;
    private final ProxyManager<String> proxyManager;

    public RateLimitingFilter(ProxyManager<String> proxyManager,RateLimitResolver rateLimitResolver) {
        this.proxyManager = proxyManager;
        this.rateLimitResolver = rateLimitResolver;
    }

    private BucketConfiguration bucketConfiguration(RateLimitPolicy policy) {
        return BucketConfiguration.builder()
                .addLimit(Bandwidth.classic(policy.capacity(), Refill.intervally(policy.capacity(), policy.refillPeriod())))
                .build();
    }


    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;
        String path = httpRequest.getRequestURI();

        RateLimitPolicy policy = rateLimitResolver.findPolicy(path).orElse(null);

        if (policy == null) {
            chain.doFilter(request, response);
            return;
        }


        String key = "rl:" + clientKey(httpRequest);
        Bucket bucket = proxyManager.builder().build(key,  () -> bucketConfiguration(policy));

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
        return ip + ":" + request.getMethod() + ":" + request.getRequestURI();
    }
}