package com.example.proxystarter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.web.client.RestTemplate;

import java.net.InetSocketAddress;
import java.net.Proxy;

@Configuration
@ConditionalOnClass(JwtDecoder.class)
@EnableConfigurationProperties(ProxyProperties.class)
public class JwtDecoderConfiguration {
    private static final Logger LOGGER = LoggerFactory.getLogger(JwtDecoderConfiguration.class);

    private final ProxyProperties proxyProperties;

    public JwtDecoderConfiguration(ProxyProperties proxyProperties) {
        this.proxyProperties = proxyProperties;
    }

    @Bean
    @ConditionalOnMissingBean
    //@ConditionalOnClass(name = "org.springframework.security.oauth2.jwt.JwtDecoder")
    //@Conditional(JwtAndProxyCondition.class)
    @ConditionalOnProperty(prefix = "spring.security.oauth2.resourceserver.jwt", name = "jwk-set-uri")
    public JwtDecoder jwtDecoder(ProxyConfigurationService proxyConfigurationService,
                                 @Value("${spring.security.oauth2.resourceserver.jwt.jwk-set-uri}") String jwkSetUri) {
        LOGGER.info("Creating proxy-enabled jwtDecoder bean");
        return createProxyEnabledJwtDecoder(jwkSetUri);
    }

    private JwtDecoder createProxyEnabledJwtDecoder(String jwkSetUri) {
        if (!proxyProperties.enabled() || proxyProperties.host() == null || proxyProperties.host().isEmpty()) {
            LOGGER.info("Creating default JwtDecoder without proxy");
            return NimbusJwtDecoder.withJwkSetUri(jwkSetUri)
                    .restOperations(new RestTemplate())
                    .build();
        }
        LOGGER.info("Creating JwtDecoder with proxy configuration");
        // Create a RestTemplate with proxy settings
        RestTemplate restTemplate = new RestTemplate();
        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setProxy(new Proxy(Proxy.Type.HTTP, new InetSocketAddress(proxyProperties.host(), proxyProperties.port())));
        restTemplate.setRequestFactory(requestFactory);

        // Create NimbusJwtDecoder with RestTemplate
        return NimbusJwtDecoder.withJwkSetUri(jwkSetUri)
                .restOperations(restTemplate)
                .build();
    }
}
