package net.detalk.api.support;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Getter
@Setter
@Configuration
@ConfigurationProperties( prefix = "detalk")
public class AppProperties {
    private String baseUrl;
    private String tokenSecret;
    private long accessTokenExpiresInSeconds;
    private long refreshTokenExpiresInSeconds;
}