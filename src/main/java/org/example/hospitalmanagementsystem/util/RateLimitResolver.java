package org.example.hospitalmanagementsystem.util;

import org.example.hospitalmanagementsystem.dto.request.RateLimitPolicy;
import org.springframework.stereotype.Component;
import org.springframework.web.util.pattern.PathPattern;
import org.springframework.web.util.pattern.PathPatternParser;
import org.springframework.http.server.PathContainer;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Component
public class RateLimitResolver {

    private final List<CompiledRule> compiledRules;

    public RateLimitResolver(Map<String, RateLimitPolicy> rateLimitsConfig) {

        PathPatternParser parser = PathPatternParser.defaultInstance;

        this.compiledRules = rateLimitsConfig.entrySet()
                .stream()
                .map(entry -> new CompiledRule(
                        parser.parse(entry.getKey()),
                        entry.getValue()
                ))
                .sorted(
                        Comparator.comparing(
                                (CompiledRule rule) ->
                                        rule.pattern()
                                                .getPatternString()
                                                .length()
                        ).reversed()
                )
                .toList();
    }

    public Optional<RateLimitPolicy> findPolicy(
            String path
    ) {

        if (path == null || path.isBlank()) {
            return Optional.empty();
        }

        PathContainer pathContainer = PathContainer.parsePath(path);

        return compiledRules.stream()
                .filter(rule ->
                        rule.pattern()
                                .matches(pathContainer)
                )
                .map(CompiledRule::policy)
                .findFirst();
    }

    private record CompiledRule(
            PathPattern pattern,
            RateLimitPolicy policy
    ) {
    }
}