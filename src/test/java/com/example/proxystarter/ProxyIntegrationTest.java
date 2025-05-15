package com.example.proxystarter;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.web.client.RestTemplate;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(classes = ProxyAutoConfiguration.class, properties = {
        "proxy.enabled=true",
        "proxy.host=proxy.example.com",
        "proxy.port=8080",
        "proxy.username=proxyuser",
        "proxy.password=proxypassword"
})
class ProxyIntegrationTest {

    @Autowired
    private ApplicationContext applicationContext;

    @Test
    void contextLoads_withProxyEnabled() {
        assertThat(applicationContext.containsBean("restTemplate")).isTrue();
        RestTemplate restTemplate = (RestTemplate) applicationContext.getBean("restTemplate");
        assertThat(restTemplate).isNotNull();
    }

    @Nested
    @SpringBootTest(classes = ProxyAutoConfiguration.class, properties = {
            "proxy.enabled=false"
    })
    class ProxyDisabledTest {

        @Autowired
        private ApplicationContext applicationContext;

        @Test
        void contextLoads_withProxyDisabled() {
            assertThat(applicationContext.containsBean("restTemplate")).isTrue(); // WITHOUT_PROXY
        }
    }
}
