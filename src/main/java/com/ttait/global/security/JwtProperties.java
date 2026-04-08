package com.ttait.global.security;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.jwt")
public record JwtProperties(
        String secretKey,
        long accessTokenValiditySeconds,
        String issuer
) {
}
