package com.solutionchallenge.tinypoof.global.config;


import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "gemini")
public class GeminiProperties {

    private  String projectId;
    private  String region;
    private  String modelName;
    private  String iss;

    private String privateKey;
}
