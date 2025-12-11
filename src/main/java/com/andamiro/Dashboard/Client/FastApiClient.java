package com.andamiro.Dashboard.Client;

import com.andamiro.Dashboard.Dto.ReportDTO.FastApiReportRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@Component
@RequiredArgsConstructor
@Slf4j
public class FastApiClient {

    private final RestTemplate restTemplate;

    @Value("${fastapi.base-url:https://unfertilising-uncontaminative-kristofer.ngrok-free.dev}")
    private String fastApiBaseUrl;

    /**
     * FastAPI 서버로 보고서 생성 요청을 보냅니다.
     * @param request FastAPI에 전달할 생성 요청 DTO
     * @return 생성된 task ID
     */
    public String generateReport(FastApiReportRequest request) {
        String url = fastApiBaseUrl + "/generate/structured";

        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            HttpEntity<FastApiReportRequest> entity = new HttpEntity<>(request, headers);

            log.info("FastAPI 보고서 생성 요청 시작: {}", url);

            ResponseEntity<Map<String, Object>> response = restTemplate.exchange(
                    url,
                    HttpMethod.POST,
                    entity,
                    new ParameterizedTypeReference<Map<String, Object>>() {}
            );

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                Map<String, Object> body = response.getBody();
                String taskId = (String) body.get("task_id");
                if (taskId != null) {
                    log.info("보고서 생성 작업 접수 완료. Task ID: {}", taskId);
                    return taskId;
                }
            }

            throw new RuntimeException("FastAPI 응답 이상: " + response.getStatusCode());

        } catch (Exception e) {
            log.error("FastAPI 보고서 생성 요청 실패", e);
            throw new RuntimeException("보고서 생성 요청 실패: " + e.getMessage(), e);
        }
    }

    /**
     * FastAPI 서버에서 생성된 PDF 보고서를 다운로드합니다.
     * @param taskId 생성된 보고서의 Task ID
     * @return PDF 파일의 바이트 배열
     */
    public byte[] downloadReport(String taskId) {
        String url = fastApiBaseUrl + "/download/" + taskId + ".pdf";

        try {
            log.info("FastAPI 보고서 다운로드 요청: {}", url);

            ResponseEntity<byte[]> response = restTemplate.getForEntity(url, byte[].class);

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                byte[] pdf = response.getBody();
                log.info("보고서 다운로드 성공. 크기: {} bytes", pdf.length);
                return pdf;
            } else {
                log.warn("FastAPI 응답 오류: {}", response.getStatusCode());
                throw new RuntimeException("보고서 다운로드 실패: 응답 상태 " + response.getStatusCode());
            }

        } catch (Exception e) {
            log.error("FastAPI 보고서 다운로드 중 오류", e);
            throw new RuntimeException("보고서 다운로드 실패: " + e.getMessage(), e);
        }
    }
}
