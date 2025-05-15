package com.example.proxystarter;

import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.env.Environment;
import org.springframework.core.type.AnnotatedTypeMetadata;

class JwtAndProxyConditionIntegrationTest {

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(ProxyAutoConfiguration.class));

    @Test
    void whenJwtUriAndProxyEnabled_thenConditionMatches() {
        contextRunner.withPropertyValues(
                "spring.security.oauth2.resourceserver.jwt.jwk-set-uri=https://example.com/jwk",
                "proxy.enabled=true"
        ).run(context -> {
            ConditionContext mockContext = Mockito.mock(ConditionContext.class);
            Environment mockEnvironment = context.getEnvironment();
            Mockito.when(mockContext.getEnvironment()).thenReturn(mockEnvironment);

            JwtAndProxyCondition condition = new JwtAndProxyCondition();
            boolean matches = condition.getMatchOutcome(mockContext, Mockito.mock(AnnotatedTypeMetadata.class)).isMatch();
            assertThat(matches).isTrue();
        });
    }

    @Test
    void whenJwtUriMissing_thenConditionDoesNotMatch() {
        contextRunner.withPropertyValues(
                "proxy.enabled=true"
        ).run(context -> {
            ConditionContext mockContext = Mockito.mock(ConditionContext.class);
            Environment mockEnvironment = context.getEnvironment();
            Mockito.when(mockContext.getEnvironment()).thenReturn(mockEnvironment);

            JwtAndProxyCondition condition = new JwtAndProxyCondition();
            boolean matches = condition.getMatchOutcome(mockContext, Mockito.mock(AnnotatedTypeMetadata.class)).isMatch();
            assertThat(matches).isFalse();
        });
    }

    @Test
    void whenProxyDisabled_thenConditionDoesNotMatch() {
        contextRunner.withPropertyValues(
                "spring.security.oauth2.resourceserver.jwt.jwk-set-uri=https://example.com/jwk",
                "proxy.enabled=false"
        ).run(context -> {
            ConditionContext mockContext = Mockito.mock(ConditionContext.class);
            Environment mockEnvironment = context.getEnvironment();
            Mockito.when(mockContext.getEnvironment()).thenReturn(mockEnvironment);

            JwtAndProxyCondition condition = new JwtAndProxyCondition();
            boolean matches = condition.getMatchOutcome(mockContext, Mockito.mock(AnnotatedTypeMetadata.class)).isMatch();
            assertThat(matches).isFalse();
        });
    }
}
