package com.example.proxystarter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

/**
 * Auto-configuration for proxy settings.
 */
@Configuration
@EnableConfigurationProperties(ProxyProperties.class)
@ConditionalOnClass(RestTemplate.class)
public class ProxyAutoConfiguration {

    private static final Logger LOGGER = LoggerFactory.getLogger(ProxyAutoConfiguration.class);

    private final ProxyProperties proxyProperties;

    public ProxyAutoConfiguration(ProxyProperties proxyProperties) {
        this.proxyProperties = proxyProperties;
    }

    @Bean
    @ConditionalOnMissingBean
    public ProxyConfigurationService proxyConfigurationService() {
        LOGGER.info("Initializing ProxyConfigurationService");
        ProxyConfigurationService service = new ProxyConfigurationService(proxyProperties);
        service.configureProxy();
        return service;
    }

    @Bean
    @ConditionalOnMissingBean(name = "proxyEnabledRestTemplate")
    @ConditionalOnProperty(prefix = "proxy", name = "enabled", havingValue = "true")
    public RestTemplate proxyEnabledRestTemplate(ProxyConfigurationService proxyConfigurationService) {
        LOGGER.info("Creating proxy-enabled RestTemplate bean");
        return proxyConfigurationService.createProxyEnabledRestTemplate();
    }
}
