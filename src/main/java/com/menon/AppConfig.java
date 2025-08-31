package com.menon;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class AppConfig
{
    @Bean
    public WebClient customerValidateWebClient(WebClient.Builder webClientBuilder)
    {
        return webClientBuilder
                .baseUrl(String.format("http://%s:%s/customer/v1/validate", "localhost", "8085"))
                .filter(new LoggingWebClientFilter())
                .build();
    }

}
