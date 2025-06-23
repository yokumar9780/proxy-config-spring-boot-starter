package com.example.proxystarter;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.web.client.RestTemplate;

import static org.assertj.core.api.Assertions.assertThat;

class ProxyAutoConfigurationTest {

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(ProxyAutoConfiguration.class))
            .withPropertyValues(
                    "proxy.enabled=true",
                    "proxy.host=proxy.example.com",
                    "proxy.port=8080",
                    "proxy.username=proxyuser",
                    "proxy.password=proxypassword"
            );

    @Test
    void whenProxyEnabled_thenProxyEnabledRestTemplateBeanCreated() {
        contextRunner.run(context -> {
            assertThat(context).hasSingleBean(RestTemplate.class);
            assertThat(context.getBean(RestTemplate.class)).isNotNull();
        });
    }

    @Test
    void whenProxyDisabled_thenProxyEnabledRestTemplateBeanNotCreated() {
        contextRunner.withPropertyValues("proxy.enabled=false")
                .run(context -> {
                    assertThat(context).hasSingleBean(ProxyConfigurationService.class);
                });
    }

    @Test
    void whenCustomRestTemplateBeanExists_thenProxyEnabledRestTemplateBeanNotCreated() {
        contextRunner.withUserConfiguration(CustomRestTemplateConfig.class)
                .run(context -> {
                    assertThat(context).hasSingleBean(RestTemplate.class);
                    assertThat(context.getBean(RestTemplate.class).getClass().getSimpleName()).isEqualTo("RestTemplate");
                });
    }

    @Test
    void whenJwtUriPropertySet_thenJwtDecoderBeanCreated() {
        contextRunner.withPropertyValues(
                        "spring.security.oauth2.resourceserver.jwt.jwk-set-uri=https://example.com/jwk",
                        "proxy.enabled=true"
                )
                .withConfiguration(AutoConfigurations.of(JwtDecoderConfiguration.class))
                .run(context -> {
                    assertThat(context).hasSingleBean(JwtDecoder.class);
                    assertThat(context.getBean(JwtDecoder.class)).isInstanceOf(JwtDecoder.class);
                });
    }

    @Test
    void whenJwtUriPropertySet_thenJwtDecoderBeanCreated_WITHOUT_PROXY() {
        contextRunner.withPropertyValues(
                "spring.security.oauth2.resourceserver.jwt.jwk-set-uri=https://example.com/jwk",
                "proxy.enabled=false"
        ).run(context -> {
            assertThat(context).doesNotHaveBean(JwtDecoder.class);
        });
    }

    @Test
    void whenJwtUriPropertySet_thenJwtDecoderBeanCreated_WITH_JWT() {
        contextRunner.withPropertyValues(
                        "spring.security.oauth2.resourceserver.jwt.jwk-set-uri=https://example.com/jwk",
                        "proxy.enabled=false"
                )
                .withConfiguration(AutoConfigurations.of(JwtDecoderConfiguration.class))
                .run(context -> {
                    assertThat(context).hasSingleBean(JwtDecoder.class);
                });
    }

    @Configuration
    static class CustomRestTemplateConfig {
        @Autowired
        @Qualifier("proxyConfigurationService")
        private ProxyConfigurationService proxyConfigurationService;

        @Bean
        public RestTemplate restTemplate() {
            RestTemplate proxyEnabledRestTemplate = proxyConfigurationService.createProxyEnabledRestTemplate();
            HttpComponentsClientHttpRequestFactory factory = (HttpComponentsClientHttpRequestFactory) proxyEnabledRestTemplate.getRequestFactory();
            factory.setConnectTimeout(5000); // 5 seconds
            factory.setReadTimeout(10000);   // 10 seconds
            return proxyEnabledRestTemplate;
        }

    }
}
