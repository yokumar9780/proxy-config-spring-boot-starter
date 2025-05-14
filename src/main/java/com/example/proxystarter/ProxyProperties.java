package com.example.proxystarter;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Properties for proxy configuration.
 */
@ConfigurationProperties(prefix = "proxy")
public record ProxyProperties(
    boolean enabled,
    String host,
    int port,
    String username,
    String password,
    String nonProxyHosts
) {
}
