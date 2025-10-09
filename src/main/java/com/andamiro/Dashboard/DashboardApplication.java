package com.andamiro.Dashboard;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import io.github.cdimascio.dotenv.Dotenv;

@SpringBootApplication
public class DashboardApplication {
	public static void main(String[] args) {
        // .env 파일을 자동 로드 (classpath나 루트에서 탐색)
        Dotenv dotenv = Dotenv.load();

        // 로드된 변수를 JVM 환경 변수로 등록 (Spring이 인식하게)
        dotenv.entries().forEach(entry -> {
            System.setProperty(entry.getKey(), entry.getValue());
        });


        SpringApplication.run(DashboardApplication.class, args);
	}
}
