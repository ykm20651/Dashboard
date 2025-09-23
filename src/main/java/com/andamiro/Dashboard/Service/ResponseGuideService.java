package com.andamiro.Dashboard.Service;

import com.andamiro.Dashboard.Dto.ResponseGuideDTO.*;
import com.andamiro.Dashboard.Entity.Incident;
import com.andamiro.Dashboard.Entity.ResponseGuide;
import com.andamiro.Dashboard.Repository.IncidentRepository;
import com.andamiro.Dashboard.Repository.ResponseGuideRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ResponseGuideService {

    private final ResponseGuideRepository responseGuideRepository;
    private final IncidentRepository incidentRepository;

    /* 04-01 API 맞춤형 대응 가이드 전략 생성 매핑 */
    @Transactional
    public ResponseGuideCreateResponse createGuide(UUID incidentId) {
        Incident incident = incidentRepository.findById(incidentId)
                .orElseThrow(() -> new IllegalArgumentException("해당 사고를 찾을 수 없습니다."));

        // 일단 샘플 가이드 하나를 DB에 저장 (추후 AI 분석 or Rule Engine 연결 가능)
        ResponseGuide guide = ResponseGuide.create(
                incident.getIncidentType(),
                "유류 유출 초기 대응 지침",
                "즉각 해상 차단막 설치",
                String.join(",", List.of("차단막 설치", "환경청 신고", "기름 회수 장비 투입")),
                "해양환경관리법 제12조"
        );

        ResponseGuide saved = responseGuideRepository.save(guide);

        return new ResponseGuideCreateResponse(
                incidentId,
                List.of(new ResponseGuideCreateResponse.Guide(
                        saved.getId(),
                        saved.getIncidentType().name(),
                        saved.getTitle(),
                        saved.getDescription(),
                        List.of(saved.getChecklist().split(",")),
                        saved.getLegalClause()
                )),
                saved.getCreatedAt()
        );
    }

    /* 04-02 API 맞춤형 대응 가이드 조회 매핑 */
    @Transactional
    public List<ResponseGuideResponse> getGuides(UUID incidentId) {
        incidentRepository.findById(incidentId)
                .orElseThrow(() -> new IllegalArgumentException("해당 사고를 찾을 수 없습니다."));

        List<ResponseGuide> guides = responseGuideRepository.findAll();

        return guides.stream()
                .map(g -> new ResponseGuideResponse(
                        g.getId(),
                        g.getIncidentType().name(),
                        g.getTitle(),
                        g.getDescription(),
                        List.of(g.getChecklist().split(",")),
                        g.getLegalClause(),
                        g.getCreatedAt()
                ))
                .collect(Collectors.toList());
    }
}
