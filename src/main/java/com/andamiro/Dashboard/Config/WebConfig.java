package com.andamiro.Dashboard.Config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOriginPatterns("*")
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                .allowedHeaders("*")
                .allowCredentials(true)
                .maxAge(3600);
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        /*docker-compose.yml - 도커 컨테이너 <-> ec2 환경에서 working_dir 여기조심해야 한다.
        * 도커 내 프로젝트 복사 폴더랑 ec2환경 폴더랑 경로가 다르니 유의하며 정적 리소스 파일 경로 생각하기
        *  */
        // 증거자료 파일 접근 허용
        registry.addResourceHandler("/files/evidence/**")
                .addResourceLocations("file:/home/ec2-user/Dashboard/uploads/evidence/");

        // AI 보고서 등 다른 파일도 함께 처리 가능
        registry.addResourceHandler("/files/reports/**")
                .addResourceLocations("file:/home/ec2-user/Dashboard/uploads/reports/");
    }

    /*
    1. 물리적 파일 저장 경로 (EC2 서버):
        ->증거자료: /home/ec2-user/Dashboard/uploads/evidence/
        ->AI 보고서: /home/ec2-user/Dashboard/uploads/reports/

    2. DB 저장 경로 (URL 형태):
        ->증거자료: /files/evidence/파일명
        ->AI 보고서: /files/reports/파일명

    */
}
