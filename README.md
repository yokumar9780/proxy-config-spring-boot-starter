# Proxy Setup Spring Boot Starter

This Spring Boot starter provides easy proxy configuration for Spring applications. It allows you to configure HTTP and
HTTPS proxy settings with authentication support.

## Features

- System-wide proxy configuration
- Support for proxy authentication
- Non-proxy hosts configuration
- Ready-to-use RestTemplate with proxy settings

## Installation

This starter is published to **GitHub Packages**.

Add the GitHub Packages repository and the dependency to your `pom.xml`:

```xml

<repositories>
    <repository>
        <id>github</id>
        <name>GitHub Packages</name>
        <url>https://maven.pkg.github.com/yokumar9780/proxy-config-spring-boot-starter</url>
    </repository>
</repositories>

<dependencies>
<dependency>
    <groupId>com.example</groupId>
    <artifactId>proxy-config-spring-boot-starter</artifactId>
    <version>1.0.0</version>
</dependency>
</dependencies>
```

💡 Make sure to replace YOUR_GITHUB_USERNAME with your actual GitHub username.

## Configuration

Add the following properties to your `application.properties` or `application.yml` file:

```properties
# Enable or disable proxy configuration
proxy.enabled=true
# Proxy host address
proxy.host=proxy.example.com
# Proxy port
proxy.port=8080
# Authentication credentials (optional)
proxy.username=proxyuser
proxy.password=proxypassword
# Non-proxy hosts (optional) - specify hosts that should bypass the proxy
proxy.non-proxy-hosts=localhost|127.0.0.1|*.example.com
```

Or in YAML format:

```yaml
proxy:
  enabled: true
  host: proxy.example.com
  port: 8080
  username: proxyuser
  password: proxypassword
  non-proxy-hosts: localhost|127.0.0.1|*.example.com
```

## Usage

Once you've added the starter to your project and configured the properties, the proxy settings will be automatically
applied.

Inject the proxied RestTemplate into your service or component:

### Accessing the ProxyConfigurationService

You can autowire the `ProxyConfigurationService` to access its functionality:

```java

@Autowired
private RestTemplate restTemplate;

// Then use it as usual:
String result = proxiedRestTemplate.getForObject("https://example.com/api", String.class);
```

## Release & Publish

To release a new version:

```bash
git tag v1.0.0
git push origin v1.0.0
```

This will trigger a GitHub Actions workflow that builds and deploys the starter to GitHub Packages.

## Building from Source

To build the project from source, you need:

- Java 21
- Spring Boot 3.4.0+
- Public GitHub repository

Run the following command:

```bash
mvn clean install
```

## Links
* [Medium.com](https://medium.com/@yokum.9780/proxy-config-spring-boot-starter-simplifying-proxy-configuration-for-spring-applications-682bc60b3fd2)

* [dev.to](https://dev.to/yogesh_kumar_9780/proxy-config-spring-boot-starter-simplifying-proxy-configuration-for-applications-2b5f)

## License

This project is licensed under the MIT License.

## Additional Documentation

For more detailed information, refer to the [ProxyConfigGuide.md](ProxyConfigGuide.md) file.
