package com.petapp.petapp.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "facebook.graph.api")
@Data
public class FacebookApiConfig {
    private String baseUrl;
    private String pageId;
    private String pageAccessToken;
    private String fields;
    private Integer limit;
}
