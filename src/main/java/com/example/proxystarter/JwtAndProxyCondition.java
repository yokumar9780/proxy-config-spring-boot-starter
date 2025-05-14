package com.example.proxystarter;

import org.springframework.boot.autoconfigure.condition.ConditionOutcome;
import org.springframework.boot.autoconfigure.condition.SpringBootCondition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.type.AnnotatedTypeMetadata;

public class JwtAndProxyCondition extends SpringBootCondition {
    @Override
    public ConditionOutcome getMatchOutcome(ConditionContext context, AnnotatedTypeMetadata metadata) {
        boolean hasJwtUri = context.getEnvironment()
                .containsProperty("spring.security.oauth2.resourceserver.jwt.jwk-set-uri");

        boolean proxyEnabled = "true".equalsIgnoreCase(
                context.getEnvironment().getProperty("proxy.enabled", "false"));

        if (hasJwtUri && proxyEnabled) {
            return ConditionOutcome.match("Both JWT URI and proxy.enabled=true are present");
        }

        return ConditionOutcome.noMatch("Missing required JWT URI or proxy.enabled");
    }
}
