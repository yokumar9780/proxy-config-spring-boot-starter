package com.example.proxystarter;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.web.client.RestTemplate;

import java.net.InetSocketAddress;
import java.net.Proxy;

@Configuration
@ConditionalOnClass(name = {"org.springframework.security.oauth2.jwt.JwtDecoder"})
public class JwtProxyConfiguration {

    @Value("${spring.security.oauth2.resourceserver.jwt.jwk-set-uri}")
    private String jwkSetUri;

    @Bean
    @ConditionalOnMissingBean
    public JwtDecoder jwtDecoder() {
        // Create a RestTemplate with proxy settings
        RestTemplate restTemplate = new RestTemplate();
        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setProxy(new Proxy(Proxy.Type.HTTP, new InetSocketAddress(proxy.host(), proxy.port())));
        restTemplate.setRequestFactory(requestFactory);

        // Create NimbusJwtDecoder with RestTemplate
        return NimbusJwtDecoder.withJwkSetUri(jwkSetUri)
                .restOperations(restTemplate)
                .build();
    }
}