package com.andamiro.Dashboard.Service;

import com.andamiro.Dashboard.Dto.ReportDTO.ReportResponse;
import com.andamiro.Dashboard.Dto.ReportDTO.FastApiReportRequest;
import com.andamiro.Dashboard.Entity.Incident;
import com.andamiro.Dashboard.Entity.Report;
import com.andamiro.Dashboard.Entity.User;
import com.andamiro.Dashboard.Repository.IncidentRepository;
import com.andamiro.Dashboard.Repository.ReportRepository;
import com.andamiro.Dashboard.Repository.UserRepository;
import com.andamiro.Dashboard.Client.FastApiClient;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;


import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class ReportService {

    private final ReportRepository reportRepository;
    private final IncidentRepository incidentRepository;
    private final UserRepository userRepository;
    private final FastApiClient fastApiClient;

    // 로컬에서는 프로젝트 루트 기준 uploads/reports/
    // 운영 서버에서는 /var/app/uploads/reports/ 같은 절대경로로 잡는 게 안전함
    private static final String UPLOAD_DIR = System.getProperty("user.dir") + "/uploads/reports/";

    /* 03-01 보고서 생성 (Owner 전용, 본인 소유 Incident에 한해서) */
    @Transactional
    public ReportResponse createReport(UUID userId, UUID incidentId) {
        if (userId == null) throw new IllegalArgumentException("인증이 필요합니다.");

        Incident incident = incidentRepository.findById(incidentId)
                .orElseThrow(() -> new EntityNotFoundException("해당 사고를 찾을 수 없습니다."));

        if (!incident.getCreator().getId().equals(userId)) {
            throw new AccessDeniedException("본인 소유 사건에 대해서만 보고서를 생성할 수 있습니다.");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("해당 사용자를 찾을 수 없습니다."));

        try {
            // * FastAPI 요청 DTO 생성
            FastApiReportRequest fastApiRequest = new FastApiReportRequest(
                    incident.getIncidentType().toString(),
                    incident.getDescription(),
                    incident.getLocation(),
                    "INITIAL",
                    "ko",  // or Locale 기반 언어
                    true,
                    "marine_laws",
                    5,
                    "gpt-4o-mini",
                    "해양 보험 청구 보고서"
            );

            String taskId = fastApiClient.generateReport(fastApiRequest);
            byte[] reportBytes = retryDownloadReport(taskId);

            // 보고서 저장
            String fileName = UUID.randomUUID() + "_AI_Report.pdf";
            Path filePath = Paths.get(UPLOAD_DIR, fileName);

            Files.createDirectories(filePath.getParent());
            Files.write(filePath, reportBytes);

            String pdfUrl = "/files/reports/" + fileName;
            Report report = Report.create(incident, user, pdfUrl);
            Report saved = reportRepository.save(report);

            return new ReportResponse(
                    saved.getId(),
                    saved.getIncident().getId(),
                    saved.getPdfUrl(),
                    saved.getGeneratedBy().getId(),
                    saved.getGeneratedAt()
            );

        } catch (Exception e) {
            log.error("AI 보고서 생성 실패", e);
            throw new RuntimeException("보고서 생성 실패: " + e.getMessage());
        }
    }


    /* 03-02 보고서 조회 (Owner 전용, 본인 소유 Incident에 한해서) */
    @Transactional
    public List<ReportResponse> getReports(UUID userId, UUID incidentId) {
        if (userId == null) {
            throw new IllegalArgumentException("인증이 필요합니다.");
        }
        
        Incident incident = incidentRepository.findById(incidentId)
                .orElseThrow(() -> new EntityNotFoundException("해당 사고를 찾을 수 없습니다."));

        // 본인 소유 사건인지 확인
        if (!incident.getCreator().getId().equals(userId)) {
            throw new AccessDeniedException("본인 소유 사건의 보고서만 조회할 수 있습니다.");
        }

        List<Report> reports = reportRepository.findAll().stream()
                .filter(r -> r.getIncident().getId().equals(incidentId))
                .toList();

        if (reports.isEmpty()) {
            throw new EntityNotFoundException("해당 사고의 보고서를 찾을 수 없습니다.");
        }

        return reports.stream()
                .map(r -> new ReportResponse(
                        r.getId(),
                        r.getIncident().getId(),
                        r.getPdfUrl(),
                        r.getGeneratedBy().getId(),
                        r.getGeneratedAt()
                ))
                .toList();
    }

    /* 03-03 보고서 삭제 */
    @Transactional
    public void deleteReport(UUID userId, UUID incidentId, UUID reportId) {
        if (userId == null) {
            throw new IllegalArgumentException("인증이 필요합니다.");
        }

        Incident incident = incidentRepository.findById((incidentId))
                .orElseThrow(() -> new EntityNotFoundException("해당 사고를 찾을 수 없습니다."));

        if (!incident.getCreator().getId().equals(userId)) {
            throw new AccessDeniedException("본인 소유 사건에 대해서만 보고서를 삭제할 수 있습니다.");
        }

        Report report = reportRepository.findById(reportId)
                .orElseThrow(() -> new EntityNotFoundException("해당 보고서를 찾을 수 없습니다."));


        // 경로 기반 파일 삭제
        try {
            String fileName = Paths.get(report.getPdfUrl()).getFileName().toString();
            Path filePath = Paths.get(System.getProperty("user.dir"), "uploads", "reports", fileName);
            Files.deleteIfExists(filePath);
        } catch (IOException e) {
            log.warn("파일 삭제 실패: {}", e.getMessage());
        }

        // DB 삭제
        reportRepository.delete(report);
        log.info("보고서 {} 삭제 완료 (incident: {})", reportId, incidentId);


    }

    public byte[] retryDownloadReport(String taskId) throws InterruptedException {
        int attempts = 5;
        for (int i = 0; i < attempts; i++) {
            try {
                return fastApiClient.downloadReport(taskId);
            } catch (Exception e) {
                log.warn("다운로드 실패, 재시도 중... ({} / {})", i + 1, attempts);
                Thread.sleep(1000L); // 1초 대기 후 재시도
            }
        }
        throw new RuntimeException("다운로드 실패: 최대 재시도 횟수 초과");
    }

}
