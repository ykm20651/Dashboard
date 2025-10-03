package com.andamiro.Dashboard.Config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        // JWT 인증 방식을 Swagger에 등록
        SecurityScheme bearerAuthScheme = new SecurityScheme()
                .type(SecurityScheme.Type.HTTP)   // HTTP 인증 방식
                .scheme("bearer")                 // Bearer 토큰
                .bearerFormat("JWT")              // JWT 포맷
                .in(SecurityScheme.In.HEADER)     // HTTP Header 에 넣음
                .name("Authorization");           // 헤더 이름

        // JWT 인증이 필요하다고 Swagger에 알리기
        SecurityRequirement securityRequirement = new SecurityRequirement().addList("bearerAuth");

        return new OpenAPI()
                .info(new Info()
                        .title("Andamiro Dashboard API")
                        .description("안다미로 사고 대응 서비스 API 문서")
                        .version("v1.0"))
                .addSecurityItem(securityRequirement)      // 전역 Security Requirement 추가
                .schemaRequirement("bearerAuth", bearerAuthScheme); // Security Scheme 추가
    }
}
