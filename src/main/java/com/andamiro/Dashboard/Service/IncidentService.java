package com.andamiro.Dashboard.Service;


import com.andamiro.Dashboard.Dto.IncidentDTO.*;
import com.andamiro.Dashboard.Entity.Incident;
import com.andamiro.Dashboard.Entity.User;
import com.andamiro.Dashboard.Repository.IncidentRepository;
import com.andamiro.Dashboard.Repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import org.springframework.security.access.AccessDeniedException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service //Controller -> Service 레이어로, DTO로 데이터 넘어온걸 실제 [1] 엔티티 클래스의 객체를 만들고 [2] (repository.save)저장하고, [3]응답 DTO로 반환하구나.
@RequiredArgsConstructor // final 붙은 멤버변수를 생성자로 생성함. 그리고 하나뿐이면 @Autowired 기본으로 적용되어 Bean 등록 가능.
public class IncidentService {
    private final IncidentRepository incidentRepository;
    private final UserRepository userRepository;

    /*
    @RequiredArgsConstructor 덕분에 이런 생성자가 자동 생성됨:
    public IncidentService(IncidentRepository incidentRepository, UserRepository userRepository) {
         this.incidentRepository = incidentRepository;
         this.userRepository = userRepository;
     }
     */

    /* 01-01 API 사고 목록 조회 매핑 */
    @Transactional(readOnly = true) //여기도 DTO에 담고
    public List<IncidentResponse> getListIncidents(UUID id) {
        List<Incident> incidents; //실제 엔티티(ERD설계한 테이블에 매핑하도록) 객체를 여기서 만들고

        if( id == null ){ //특정 사용자가 없으면, 그냥 등록된 사고 목록 조회. . .
            incidents = incidentRepository.findAll();
        }else{ //id가 있으면 특정 사용자 (userId, 즉, creatorId)가 등록한 사고만 List에 답기.
            incidents = incidentRepository.findByCreatorId(id);
        }

        return incidents.stream()//여기서 만든 엔티티 객체에 정보를 넣고 반환. 마지막에 List -> stream으로 바꿔서 좀더
                //데이터 변형하고 순회하기 좋은게 stream객체라 이거 써서 데이터 넣고 해서, List로 마지막에 반환함
                .map(i -> new IncidentResponse(
                        i.getId(),
                        i.getTitle(),
                        i.getIncidentType().name().toLowerCase(),
                        i.getLocation(),
                        i.getDescription(),
                        i.getHappenedAt(),
                        i.getReportedAt(),
                        i.getStatus().name().toLowerCase(),
                        new IncidentResponse.CreatorSummary(
                                i.getCreator().getId(),
                                i.getCreator().getName()
                        )
                ))
                .toList();
    }

    /* 01-02 API 사고 등록 매핑 */
    @Transactional
    public IncidentResponse createIncident(UUID userId, IncidentCreateRequest request) {
        // ✅ SecurityContext에서 현재 로그인한 사용자 ID 가져오기
        // Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        // UUID userId = (UUID) auth.getPrincipal();

        //Service 레이어에서 DTO로 데이터 넘어온걸 실제 [1] 엔티티 클래스의 객체를 만들고 [2] (repository.save)저장하고, [3]응답 DTO로 반환하구나.
        User creator = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        Incident incident = Incident.create(
                creator, // 나중에 creator(User) 넣어줄 부분
                request.title(),
                request.description(),
                Incident.IncidentType.valueOf(request.incidentType().toUpperCase()),
                request.location(),
                request.happenedAt()
        );

        incidentRepository.save(incident);

        return new IncidentResponse(
                incident.getId(),
                incident.getTitle(),
                incident.getIncidentType().name().toLowerCase(),
                incident.getLocation(),
                incident.getDescription(),
                incident.getHappenedAt(),
                incident.getReportedAt(),
                incident.getStatus().name().toLowerCase(),
                new IncidentResponse.CreatorSummary(
                        incident.getCreator().getId(),
                        incident.getCreator().getName()
                )
        );
    }

    /* 01-03 사고 상세 조회 (Owner 전용, 본인 소유만) */
    @Transactional(readOnly = true)
    public IncidentDetailResponse getDetailIncident(UUID userId, UUID incidentId) {
        // evidenceFiles, reports 를 fetch join 으로 가져오기
        Incident incident = incidentRepository.findByIdWithDetails(incidentId)
                .orElseThrow(() -> new EntityNotFoundException("해당 사고 없음"));

        // 본인 소유 여부 검증
        if (!incident.getCreator().getId().equals(userId)) {
            throw new AccessDeniedException("본인 소유 사건만 조회할 수 있습니다.");
        }

        // EvidenceFile 매핑
        List<IncidentDetailResponse.EvidenceFile> evidenceResponses = incident.getEvidenceFiles().stream()
                .filter(e -> !e.isDeleted()) // 삭제되지 않은 파일만
                .map(e -> new IncidentDetailResponse.EvidenceFile(
                        e.getId(),
                        e.getFileUrl(),
                        e.getFileType().name().toLowerCase(),
                        e.getDescription(),
                        e.getUploader().getId()
                ))
                .toList();

        // Report 매핑
        List<IncidentDetailResponse.Report> reportResponses = incident.getReports().stream()
                .map(r -> new IncidentDetailResponse.Report(
                        r.getId(),
                        r.getPdfUrl(),
                        r.getGeneratedAt()
                ))
                .toList();

        // 최종 응답 반환
        return new IncidentDetailResponse(
                incident.getId(),
                incident.getTitle(),
                incident.getDescription(),
                incident.getIncidentType().name().toLowerCase(),
                incident.getLocation(),
                incident.getHappenedAt(),
                incident.getReportedAt(),
                incident.getStatus().name().toLowerCase(),
                new IncidentDetailResponse.Creator(
                        incident.getCreator().getId(),
                        incident.getCreator().getName()
                ),
                evidenceResponses,
                reportResponses
        );
    }



    /* 01-04 사고 수정 (Owner 전용, 본인 소유만) */
    @Transactional
    public IncidentUpdateResponse updateIncident(UUID userId, UUID incidentId, IncidentUpdateRequest request) {
        Incident incident = incidentRepository.findById(incidentId)
                .orElseThrow(() -> new EntityNotFoundException("해당 사고 없음"));

        if (!incident.getCreator().getId().equals(userId)) {
            throw new AccessDeniedException("본인 소유 사건만 수정할 수 있습니다.");
        }

        incident.update(request.title(), request.description(), request.location());
        incidentRepository.save(incident);

        return new IncidentUpdateResponse(
                incident.getId(),
                incident.getTitle(),
                incident.getDescription(),
                incident.getIncidentType().name().toLowerCase(),
                incident.getLocation(),
                incident.getHappenedAt(),
                LocalDateTime.now()
        );
    }

    /* 01-05 사고 삭제 (Owner 전용, 본인 소유만) */
    @Transactional
    public void deleteIncident(UUID userId, UUID incidentId) {
        Incident incident = incidentRepository.findById(incidentId)
                .orElseThrow(() -> new EntityNotFoundException("해당 사고 없음"));

        if (!incident.getCreator().getId().equals(userId)) {
            throw new AccessDeniedException("본인 소유 사건만 삭제할 수 있습니다.");
        }

        incidentRepository.delete(incident);
    }


}
