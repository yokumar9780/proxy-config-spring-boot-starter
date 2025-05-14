package com.example.proxystarter;


import org.apache.hc.client5.http.auth.AuthScope;
import org.apache.hc.client5.http.auth.UsernamePasswordCredentials;
import org.apache.hc.client5.http.impl.auth.BasicCredentialsProvider;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.core5.http.HttpHost;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestTemplate;

import java.net.Authenticator;
import java.net.InetSocketAddress;
import java.net.PasswordAuthentication;
import java.net.Proxy;

/**
 * Service responsible for configuring system-wide proxy settings.
 */
public class ProxyConfigurationService {

    private static final Logger LOGGER = LoggerFactory.getLogger(ProxyConfigurationService.class);

    private final ProxyProperties proxyProperties;

    public ProxyConfigurationService(ProxyProperties proxyProperties) {
        this.proxyProperties = proxyProperties;
    }

    /**
     * Initializes the proxy configuration based on properties.
     */
    public void configureProxy() {
        if (!proxyProperties.enabled() || proxyProperties.host() == null || proxyProperties.host().isEmpty()) {
            LOGGER.info("Proxy configuration is disabled or incomplete. No proxy settings will be applied.");
            return;
        }

        LOGGER.info("Configuring proxy with host: {} and port: {}", proxyProperties.host(), proxyProperties.port());

        // Set system properties
        System.setProperty("http.proxyHost", proxyProperties.host());
        System.setProperty("http.proxyPort", String.valueOf(proxyProperties.port()));
        System.setProperty("https.proxyHost", proxyProperties.host());
        System.setProperty("https.proxyPort", String.valueOf(proxyProperties.port()));

        // Set non-proxy hosts if specified
        if (proxyProperties.nonProxyHosts() != null && !proxyProperties.nonProxyHosts().isEmpty()) {
            System.setProperty("http.nonProxyHosts", proxyProperties.nonProxyHosts());
            System.setProperty("https.nonProxyHosts", proxyProperties.nonProxyHosts());
        }

        // Configure authentication if credentials are provided
        if (proxyProperties.username() != null && !proxyProperties.username().isEmpty() &&
                proxyProperties.password() != null && !proxyProperties.password().isEmpty()) {

            LOGGER.info("Configuring proxy authentication for user: {}", proxyProperties.username());

            System.setProperty("http.proxyUser", proxyProperties.username());
            System.setProperty("http.proxyPassword", proxyProperties.password());
            System.setProperty("https.proxyUser", proxyProperties.username());
            System.setProperty("https.proxyPassword", proxyProperties.password());

            // Set authenticator for Java applications
            Authenticator.setDefault(new Authenticator() {
                @Override
                protected PasswordAuthentication getPasswordAuthentication() {
                    if (getRequestingHost().equalsIgnoreCase(proxyProperties.host()) &&
                            getRequestingPort() == proxyProperties.port()) {
                        return new PasswordAuthentication(
                                proxyProperties.username(),
                                proxyProperties.password().toCharArray()
                        );
                    }
                    return null;
                }
            });
        }

        LOGGER.info("Proxy configuration completed successfully");
    }

    /**
     * Creates a RestTemplate configured with the proxy settings.
     *
     * @return a RestTemplate with proxy configuration applied
     */
    public RestTemplate createProxyEnabledRestTemplate() {
        if (!proxyProperties.enabled() || proxyProperties.host() == null || proxyProperties.host().isEmpty()) {
            LOGGER.info("Creating default RestTemplate without proxy");
            return new RestTemplate();
        }

        LOGGER.info("Creating RestTemplate with proxy configuration");
        CloseableHttpClient httpClient = getCloseableHttpClient();
        HttpComponentsClientHttpRequestFactory factory = new HttpComponentsClientHttpRequestFactory(httpClient);
        return new RestTemplate(factory);
    }


    public RestClient createProxyEnabledRestClient() {

        if (!proxyProperties.enabled() || proxyProperties.host() == null || proxyProperties.host().isEmpty()) {
            LOGGER.info("Creating default RestClient without proxy");
            return RestClient.builder()
                    .defaultHeaders(headers -> headers.set("Accept", "application/json"))
                    .requestFactory(new HttpComponentsClientHttpRequestFactory())
                    .build();
        }

        LOGGER.info("Creating RestClient with proxy configuration");

        CloseableHttpClient httpClient = getCloseableHttpClient();
        HttpComponentsClientHttpRequestFactory factory = new HttpComponentsClientHttpRequestFactory(httpClient);

        return RestClient.builder()
                .defaultHeaders(headers -> headers.set("Accept", "application/json"))
                .requestFactory(factory)
                .build();
    }


    public JwtDecoder createProxyEnabledJwtDecoder(String jwkSetUri) {
        if (!proxyProperties.enabled() || proxyProperties.host() == null || proxyProperties.host().isEmpty()) {
            LOGGER.info("Creating default JwtDecoder without proxy");
            return  NimbusJwtDecoder.withJwkSetUri(jwkSetUri)
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

    private CloseableHttpClient getCloseableHttpClient() {
        HttpHost proxy = new HttpHost(proxyProperties.host(), proxyProperties.port());
        CloseableHttpClient httpClient = HttpClients.custom()
                .setProxy(proxy)
                .build();

        // Configure authentication if credentials are provided
        if (proxyProperties.username() != null && !proxyProperties.username().isEmpty() &&
                proxyProperties.password() != null && !proxyProperties.password().isEmpty()) {

            BasicCredentialsProvider credentialsProvider = new BasicCredentialsProvider();
            credentialsProvider.setCredentials(
                    new AuthScope(proxyProperties.host(), proxyProperties.port()),
                    new UsernamePasswordCredentials(proxyProperties.username(), proxyProperties.password().toCharArray())
            );
            httpClient = HttpClients.custom()
                    .setProxy(proxy)
                    .setDefaultCredentialsProvider(credentialsProvider)
                    .build();
        }
        return httpClient;
    }

}
