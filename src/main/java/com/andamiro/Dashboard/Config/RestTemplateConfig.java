package com.andamiro.Dashboard.Config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

@Configuration
public class RestTemplateConfig {

    @Bean
    public RestTemplate restTemplate() {
        //Spring에서 제공하는 HTTP 클라이언트임. 다른 서버와 hTTP 통신할 때 사용함. 
        return new RestTemplate();
    }
}
