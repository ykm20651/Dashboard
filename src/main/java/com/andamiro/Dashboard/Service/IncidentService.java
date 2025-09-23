package com.andamiro.Dashboard.Service;


import com.andamiro.Dashboard.Dto.IncidentDTO.*;
import com.andamiro.Dashboard.Entity.Incident;
import com.andamiro.Dashboard.Repository.IncidentRepository;
import com.andamiro.Dashboard.Repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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

    //01-01 : 사고 목록 조회
    @Transactional(readOnly = true)
    public List<IncidentResponse> getListIncidents(UUID id) {
        List<Incident> incidents;

        if( id == null ){
            incidents = incidentRepository.findAll();
        }else{
            incidents = incidentRepository.findByCreatorId(id);
        }

        return incidents.stream()
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

    // 01-02: 사고 등록
    @Transactional
    public IncidentResponse createIncident(IncidentCreateRequest request) {
        //Service 레이어에서 DTO로 데이터 넘어온걸 실제 [1] 엔티티 클래스의 객체를 만들고 [2] (repository.save)저장하고, [3]응답 DTO로 반환하구나.
        Incident incident = Incident.create(
                null, // 나중에 creator(User) 넣어줄 부분
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

    // 01-03: 사고 상세 조회
    @Transactional
    public IncidentDetailResponse getDetailIncident(UUID id) {
        Incident incident = incidentRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("해당 사고 없음"));

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
                // EvidenceFile과 Report 매핑은 나중에 추가
                java.util.List.of(),
                java.util.List.of()
        );
    }

    // 01-04: 사고 수정
    @Transactional
    public IncidentUpdateResponse updateIncident(UUID id, IncidentUpdateRequest request) {
        Incident incident = incidentRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("해당 사고 없음"));

        incident.update(request.title(), request.description(), request.location());

        incidentRepository.save(incident);

        return new IncidentUpdateResponse(
                incident.getId(),
                incident.getTitle(),
                incident.getDescription(),
                incident.getIncidentType().name().toLowerCase(),
                incident.getLocation(),
                incident.getHappenedAt(),
                incident.getUpdatedAt()
        );
    }

    // 01-05: 사고 삭제
    @Transactional
    public void deleteIncident(UUID id) {
        if( ! incidentRepository.existsById(id) ){
            throw new IllegalArgumentException("사고를 찾을 수 없습니다. ID = " + id);
        }
        incidentRepository.deleteById(id);
    }


}
