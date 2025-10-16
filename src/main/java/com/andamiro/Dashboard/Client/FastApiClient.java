package com.andamiro.Dashboard.Client;

import com.andamiro.Dashboard.Dto.ReportDTO.FastApiReportRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@Component
@RequiredArgsConstructor
@Slf4j
public class FastApiClient {

    private final RestTemplate restTemplate;
    
    @Value("${fastapi.base-url:http://localhost:8000}")
    private String fastApiBaseUrl;

    /**
     * FastAPI 서버로 보고서 생성 요청을 보냅니다.
     */
    public String generateReport(FastApiReportRequest request) {
        try {
            String url = fastApiBaseUrl + "/generate/insurance"; //FastAPI 서버의 보고서 생성 url
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            
            //HttpEntity -> HTTP 요청 하나를 표현하는 객체임. HttpEntity<T>(body, headers)
            HttpEntity<FastApiReportRequest> entity = new HttpEntity<>(request, headers);
            
            log.info("FastAPI 서버로 보고서 생성 요청: {}", url);
            

            //restTemplate.exchange -> HTTP 요청을 보내고 응답을 받는 실제 통신 메서드 
            ResponseEntity<Map<String, Object>> response = restTemplate.exchange(
                url, 
                HttpMethod.POST, 
                entity, 
                new ParameterizedTypeReference<Map<String, Object>>() {}
            );
            
            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                Map<String, Object> body = response.getBody();
                if (body != null) {
                    String taskId = (String) body.get("task_id");
                    log.info("보고서 생성 작업 시작됨. Task ID: {}", taskId);
                    return taskId;
                }
            }
            throw new RuntimeException("FastAPI 서버 응답 오류: " + response.getStatusCode());
            
        } catch (Exception e) {
            log.error("FastAPI 서버 통신 오류", e);
            throw new RuntimeException("보고서 생성 요청 실패: " + e.getMessage(), e);
        }
    }

    /**
     * FastAPI 서버에서 생성된 보고서 파일을 다운로드합니다.
     */
    public byte[] downloadReport(String taskId) {
        try {
            String url = fastApiBaseUrl + "/download/" + taskId + ".pdf";
            
            log.info("FastAPI 서버에서 보고서 다운로드: {}", url);
            
            ResponseEntity<byte[]> response = restTemplate.getForEntity(url, byte[].class);
            
            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                byte[] body = response.getBody();
                log.info("보고서 다운로드 완료. 크기: {} bytes", body.length);
                return body;
            } else {
                throw new RuntimeException("FastAPI 서버 응답 오류: " + response.getStatusCode());
            }
            
        } catch (Exception e) {
            log.error("FastAPI 서버 다운로드 오류", e);
            throw new RuntimeException("보고서 다운로드 실패: " + e.getMessage(), e);
        }
    }
}
