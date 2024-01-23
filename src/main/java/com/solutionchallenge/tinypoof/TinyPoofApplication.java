package com.solutionchallenge.tinypoof;

import com.solutionchallenge.tinypoof.global.config.GeminiProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties(GeminiProperties.class)
public class TinyPoofApplication {

    public static void main(String[] args) {
        SpringApplication.run(TinyPoofApplication.class, args);
    }

}
