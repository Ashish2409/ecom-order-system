package com.ashish.ecom.order_service.config;


import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class WebClientConfig {

    @Value("${services.product-url}")
    private String productUrl;

    @Bean(name = "productWebClient")
    public WebClient productWebClient(WebClient.Builder builder) {
        return builder
                .baseUrl(productUrl)
                .build();
    }
}
