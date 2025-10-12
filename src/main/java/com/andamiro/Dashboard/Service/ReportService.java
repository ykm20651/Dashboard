package com.andamiro.Dashboard.Service;

import com.andamiro.Dashboard.Dto.ReportDTO.ReportResponse;
import com.andamiro.Dashboard.Entity.Incident;
import com.andamiro.Dashboard.Entity.Report;
import com.andamiro.Dashboard.Entity.User;
import com.andamiro.Dashboard.Repository.IncidentRepository;
import com.andamiro.Dashboard.Repository.ReportRepository;
import com.andamiro.Dashboard.Repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ReportService {

    private final ReportRepository reportRepository;
    private final IncidentRepository incidentRepository;
    private final UserRepository userRepository;

    // ✅ 로컬에서는 프로젝트 루트 기준 uploads/reports/
    // 운영 서버에서는 /var/app/uploads/reports/ 같은 절대경로로 잡는 게 안전함
    private static final String UPLOAD_DIR = System.getProperty("user.dir") + "/uploads/reports/";

    /* 03-01 보고서 생성 (Owner 전용, 본인 소유 Incident에 한해서) */
    @Transactional
    public ReportResponse createReport(UUID userId, UUID incidentId, MultipartFile file) {
        if (userId == null) {
            throw new IllegalArgumentException("인증이 필요합니다.");
        }

        //1. 파일을 서버 로컬 디렉토리(uploads/reports/) 에 저장 (지금 구현한 방식).
        //2. 파일을 AWS S3 같은 외부 스토리지 에 업로드 후 URL 저장.
        // Incident 조회
        Incident incident = incidentRepository.findById(incidentId)
                .orElseThrow(() -> new EntityNotFoundException("해당 사고를 찾을 수 없습니다."));

        // 본인 소유 사건인지 확인
        if (!incident.getCreator().getId().equals(userId)) {
            throw new AccessDeniedException("본인 소유 사건에 대해서만 보고서를 생성할 수 있습니다.");
        }

        // 사용자 조회
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("해당 사용자를 찾을 수 없습니다."));

        // 파일 저장
        String fileName = UUID.randomUUID() + "_" + file.getOriginalFilename();
        Path filePath = Paths.get(UPLOAD_DIR, fileName);

        try {
            Files.createDirectories(filePath.getParent());
            file.transferTo(filePath.toFile());
        } catch (IOException e) {
            throw new RuntimeException("파일 저장 실패", e);
        }

        // DB 저장
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
}
