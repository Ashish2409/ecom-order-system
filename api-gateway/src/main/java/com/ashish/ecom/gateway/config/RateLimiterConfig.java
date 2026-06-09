package com.ashish.ecom.gateway.config;

import org.springframework.cloud.gateway.filter.ratelimit.KeyResolver;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import reactor.core.publisher.Mono;

/**
 * Rate Limiter Configuration
 * Defines how to resolve the key for rate limiting
 */
@Configuration
public class RateLimiterConfig {

    /**
     * Key resolver for rate limiting
     * Uses IP address as the key (can be changed to user ID for authenticated requests)
     */
    @Bean
    public KeyResolver ipKeyResolver() {
        return exchange -> {
            String ip = exchange.getRequest().getRemoteAddress() != null 
                    ? exchange.getRequest().getRemoteAddress().getAddress().getHostAddress()
                    : "unknown";
            return Mono.just(ip);
        };
    }
}
