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

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class EvidenceFileService {

    private final EvidenceFileRepository evidenceFileRepository;
    private final IncidentRepository incidentRepository;
    private final UserRepository userRepository;

    /* 02-01 API 해당 사고의 증거자료 목록 조회 매핑 */
    public List<EvidenceFileResponse> getEvidenceFiles(UUID incidentId) {
        Incident incident = incidentRepository.findById(incidentId)
                .orElseThrow(() -> new IllegalArgumentException("해당 사고를 찾을 수 없습니다."));

        return evidenceFileRepository.findByIncidentAndIsDeletedFalse(incident).stream()
                .map(e -> new EvidenceFileResponse(
                        e.getId(),
                        e.getFileUrl(),
                        e.getFileType().name().toLowerCase(),
                        e.getDescription(),
                        e.getUploader().getId(),
                        e.getCreatedAt()
                ))
                .toList();
    }

    /* 02-02 API 증거자료 추가 업로드 매핑 */
    @Transactional
    public EvidenceFileCreateResponse uploadEvidenceFile(UUID incidentId, UUID uploaderId, EvidenceFileCreateRequest req) {
        Incident incident = incidentRepository.findById(incidentId)
                .orElseThrow(() -> new IllegalArgumentException("해당 사고를 찾을 수 없습니다."));
        User uploader = userRepository.findById(uploaderId)
                .orElseThrow(() -> new IllegalArgumentException("해당 사용자를 찾을 수 없습니다."));

        EvidenceFile file = EvidenceFile.create(
                incident,
                uploader,
                req.fileUrl(),
                EvidenceFile.FileType.valueOf(req.fileType().toUpperCase())
        );

        file = evidenceFileRepository.save(file);

        return new EvidenceFileCreateResponse(
                file.getId(),
                incident.getId(),
                file.getFileUrl(),
                file.getFileType().name().toLowerCase(),
                file.getDescription(),
                uploader.getId(),
                file.getCreatedAt()
        );
    }

    /* 02-03 API 증거자료 삭제 매핑 */
    @Transactional
    public void deleteEvidenceFile(UUID fileId) {
        EvidenceFile file = evidenceFileRepository.findById(fileId)
                .orElseThrow(() -> new IllegalArgumentException("해당 증거자료를 찾을 수 없습니다."));
        file.delete();
    }

    /* 02-04 API 증거자료 수정 매핑 */
    @Transactional
    public EvidenceFileUpdateResponse updateEvidenceFile(UUID fileId, EvidenceFileUpdateRequest req) {
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
