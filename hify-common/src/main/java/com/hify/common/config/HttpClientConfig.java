package com.hify.common.config;

import com.hify.common.http.LlmHttpClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class HttpClientConfig {

    @Bean
    public LlmHttpClient llmHttpClient() {
        return new LlmHttpClient();
    }
}
