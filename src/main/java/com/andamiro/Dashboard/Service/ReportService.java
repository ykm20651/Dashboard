package com.andamiro.Dashboard.Service;

import com.andamiro.Dashboard.Dto.ReportDTO.ReportResponse;
import com.andamiro.Dashboard.Entity.Incident;
import com.andamiro.Dashboard.Entity.Report;
import com.andamiro.Dashboard.Entity.User;
import com.andamiro.Dashboard.Repository.IncidentRepository;
import com.andamiro.Dashboard.Repository.ReportRepository;
import com.andamiro.Dashboard.Repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ReportService {

    private final ReportRepository reportRepository;
    private final IncidentRepository incidentRepository;
    private final UserRepository userRepository;

    private static final String UPLOAD_DIR = "uploads/reports/"; // PDF 저장 폴더

    /* 03-01 API 보고서 생성 매핑 MultipartFile(파일 업로드 방식)*/
    @Transactional
    public ReportResponse createReport(UUID incidentId, UUID userId, MultipartFile file) {
        //1. 파일을 서버 로컬 디렉토리(uploads/reports/) 에 저장 (지금 구현한 방식).
        //2. 파일을 AWS S3 같은 외부 스토리지 에 업로드 후 URL 저장.

        Incident incident = incidentRepository.findById(incidentId)
                .orElseThrow(() -> new IllegalArgumentException("해당 사고를 찾을 수 없습니다."));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("해당 사용자를 찾을 수 없습니다."));

        // 파일 저장
        String fileName = UUID.randomUUID() + "_" + file.getOriginalFilename();
        Path filePath = Paths.get(UPLOAD_DIR, fileName);

        try {
            Files.createDirectories(filePath.getParent()); // 폴더 없으면 생성
            file.transferTo(filePath.toFile());
        } catch (IOException e) {
            throw new RuntimeException("파일 저장 실패", e);
        }

        // DB 저장
        String pdfUrl = "/files/reports/" + fileName; // 나중에 CDN 또는 S3로 교체 가능
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

    /* 03-02 API 보고서 조회 매핑 */
    public List<ReportResponse> getReports(UUID incidentId) {
        List<Report> reports = reportRepository.findAll().stream()
                .filter(r -> r.getIncident().getId().equals(incidentId))
                .toList();

        if (reports.isEmpty()) {
            throw new IllegalArgumentException("해당 사고의 보고서를 찾을 수 없습니다.");
        }

        return reports.stream()
                .map(r -> new ReportResponse(
                        r.getId(),
                        r.getIncident().getId(),
                        r.getPdfUrl(),
                        r.getGeneratedBy().getId(),
                        r.getGeneratedAt()
                ))
                .collect(Collectors.toList());
    }
}
