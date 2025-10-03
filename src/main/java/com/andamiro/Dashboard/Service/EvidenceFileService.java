package com.andamiro.Dashboard.Service;

import com.andamiro.Dashboard.Dto.EvidenceFileDTO.*;
import com.andamiro.Dashboard.Entity.EvidenceFile;
import com.andamiro.Dashboard.Entity.Incident;
import com.andamiro.Dashboard.Entity.User;
import com.andamiro.Dashboard.Repository.EvidenceFileRepository;
import com.andamiro.Dashboard.Repository.IncidentRepository;
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

@Service
@RequiredArgsConstructor
public class EvidenceFileService {

    private final EvidenceFileRepository evidenceFileRepository;
    private final IncidentRepository incidentRepository;
    private final UserRepository userRepository;
    // ✅ 로컬에서는 프로젝트 루트 기준 uploads/reports/
    // 운영 서버에서는 /var/app/uploads/reports/ 같은 절대경로로 잡는 게 안전함
    private static final String UPLOAD_DIR = System.getProperty("user.dir") + "/uploads/evidence/";


    /* 02-01 API 해당 사고의 증거자료 목록 조회 */
    public List<EvidenceFileResponse> getEvidenceFiles(UUID incidentId) {
        Incident incident = incidentRepository.findById(incidentId)
                .orElseThrow(() -> new IllegalArgumentException("해당 사고를 찾을 수 없습니다."));

        return evidenceFileRepository.findByIncidentAndIsDeletedFalse(incident).stream()
                .map(e -> new EvidenceFileResponse(
                        e.getId(),
                        e.getIncident().getId(),
                        e.getFileUrl(),
                        e.getFileType().name().toLowerCase(),
                        e.getDescription(),
                        e.getUploader().getId(),
                        e.getCreatedAt()
                ))
                .toList();
    }

    /* 02-02 API 증거자료 업로드 (파일 저장) */
    @Transactional
    public EvidenceFileCreateResponse uploadEvidenceFile(UUID incidentId, UUID userId, MultipartFile file, String description) {
        Incident incident = incidentRepository.findById(incidentId)
                .orElseThrow(() -> new IllegalArgumentException("해당 사고를 찾을 수 없습니다."));
        User uploader = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("해당 사용자를 찾을 수 없습니다."));

        // 파일 저장
        String fileName = UUID.randomUUID() + "_" + file.getOriginalFilename();
        Path filePath = Paths.get(UPLOAD_DIR, fileName);

        try {
            Files.createDirectories(filePath.getParent()); // 디렉토리 없으면 생성
            file.transferTo(filePath.toFile());
        } catch (IOException e) {
            throw new RuntimeException("증거 파일 저장 실패", e);
        }

        // 파일 타입 구분 (image / video)
        String contentType = file.getContentType();
        EvidenceFile.FileType fileType = (contentType != null && contentType.startsWith("video"))
                ? EvidenceFile.FileType.VIDEO
                : EvidenceFile.FileType.IMAGE;

        // DB 저장
        String fileUrl = "/files/evidence/" + fileName; // static 매핑
        EvidenceFile evidenceFile = EvidenceFile.create(
                incident,
                uploader,
                fileUrl,
                fileType
        );
        evidenceFile.updateDescription(description);

        evidenceFile = evidenceFileRepository.save(evidenceFile);

        return new EvidenceFileCreateResponse(
                evidenceFile.getId(),
                incident.getId(),
                evidenceFile.getFileUrl(),
                evidenceFile.getFileType().name().toLowerCase(),
                evidenceFile.getDescription(),
                uploader.getId(),
                evidenceFile.getCreatedAt()
        );
    }

    /* 02-03 API 증거자료 삭제 */
    @Transactional
    public void deleteEvidenceFile(UUID userId, UUID fileId) {
        userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("해당 사용자를 찾을 수 없습니다."));

        EvidenceFile file = evidenceFileRepository.findById(fileId)
                .orElseThrow(() -> new IllegalArgumentException("해당 증거자료를 찾을 수 없습니다."));
        file.delete();
    }

    /* 02-04 API 증거자료 수정 */
    @Transactional
    public EvidenceFileUpdateResponse updateEvidenceFile(UUID userId, UUID fileId, EvidenceFileUpdateRequest req) {
        userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("해당 사용자를 찾을 수 없습니다."));

        EvidenceFile file = evidenceFileRepository.findById(fileId)
                .orElseThrow(() -> new IllegalArgumentException("해당 증거자료를 찾을 수 없습니다."));
        file.updateDescription(req.description());

        return new EvidenceFileUpdateResponse(
                file.getId(),
                file.getIncident().getId(),
                file.getFileUrl(),
                file.getFileType().name().toLowerCase(),
                file.getDescription(),
                file.getDeletedAt() != null ? file.getDeletedAt() : file.getCreatedAt()
        );
    }
}
