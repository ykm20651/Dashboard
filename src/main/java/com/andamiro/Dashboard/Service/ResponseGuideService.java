package com.andamiro.Dashboard.Service;

import com.andamiro.Dashboard.Dto.ResponseGuideDTO.*;
import com.andamiro.Dashboard.Entity.Incident;
import com.andamiro.Dashboard.Entity.ResponseGuide;
import com.andamiro.Dashboard.Repository.IncidentRepository;
import com.andamiro.Dashboard.Repository.ResponseGuideRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ResponseGuideService {

    private final ResponseGuideRepository responseGuideRepository;
    private final IncidentRepository incidentRepository;

    /* 04-01 API 맞춤형 대응 가이드 전략 생성 (Owner 전용, 본인 소유 사건만 가능) */
    @Transactional
    public ResponseGuideCreateResponse createGuide(UUID userId, UUID incidentId) {
        Incident incident = incidentRepository.findById(incidentId)
                .orElseThrow(() -> new IllegalArgumentException("해당 사고를 찾을 수 없습니다."));

        // 본인 소유 사건 검증
        if (!incident.getCreator().getId().equals(userId)) {
            throw new AccessDeniedException("본인 소유 사건에 대해서만 대응 가이드를 생성할 수 있습니다.");
        }

        ResponseGuide guide;

        switch (incident.getIncidentType()) {
            case FIRE:
                guide = ResponseGuide.create(
                        incident.getIncidentType(),
                        "선박 화재 대응 매뉴얼",
                        "선박 내 화재 발생 시 인명 보호와 초기 진화에 최우선",
                        String.join(",", List.of(
                                "1단계: 화재 발생 위치 확인 및 선원 전원 대피",
                                "2단계: 선내 비상경보 발령 및 화재 진압팀 편성",
                                "3단계: 휴대용 소화기 또는 고정식 CO₂ 소화설비 사용",
                                "4단계: 전력 차단 및 기관실 연료 공급 차단",
                                "5단계: 화재가 통제 불가 시 즉시 해양경찰 신고"
                        )),
                        "선박안전법 제45조, SOLAS(국제해상인명안전협약) Chapter II-2"
                );
                break;

            case COLLISION:
                guide = ResponseGuide.create(
                        incident.getIncidentType(),
                        "선박 충돌 대응 매뉴얼",
                        "충돌 직후 피해 상황을 확인하고 추가 사고 방지",
                        String.join(",", List.of(
                                "1단계: 충돌 지점 격리 및 손상 부위 누수 여부 점검",
                                "2단계: 기관실, 화물창 등 주요 구역 방수문/격벽 점검",
                                "3단계: 피해 선박과 무전 통신 유지 및 피난 협조",
                                "4단계: 항해일지 및 VDR(항해기록장치)에 사고 기록",
                                "5단계: 관제센터(VTS) 및 해양경찰 즉시 보고"
                        )),
                        "국제해상충돌예방규칙 제8조, 해사안전법 제29조"
                );
                break;

            case OIL_SPILL:
                guide = ResponseGuide.create(
                        incident.getIncidentType(),
                        "유류 유출 대응 매뉴얼",
                        "해양오염 확산을 차단하고 신속한 방제 조치",
                        String.join(",", List.of(
                                "1단계: 즉시 유류 유출 지점 확인 및 차단막 설치",
                                "2단계: 흡착재, 유류 회수기 등 방제 장비 투입",
                                "3단계: 환경청(해양환경공단) 및 항만청 긴급 보고",
                                "4단계: 유출량 추정 및 항해일지 기록",
                                "5단계: 방제업체 지원 요청 및 2차 오염 차단"
                        )),
                        "해양환경관리법 제12조, MARPOL(국제해양오염방지협약) Annex I"
                );
                break;

            case CREW_INJURY:
                guide = ResponseGuide.create(
                        incident.getIncidentType(),
                        "선원 부상 대응 매뉴얼",
                        "부상자의 생명을 보호하고 신속한 의료 지원 확보",
                        String.join(",", List.of(
                                "1단계: 즉시 응급 처치 실시 (지혈, 기도 확보, CPR 등)",
                                "2단계: 부상 정도 평가 후 환자 안정화",
                                "3단계: 선내 무전으로 원격 의료지원 요청 (TMAS 등)",
                                "4단계: 항구 접근 가능 여부 검토 및 응급 후송 준비",
                                "5단계: 항만청·해양경찰에 의료 후송 협조 요청"
                        )),
                        "산업안전보건법 제26조, 국제해사기구(IMO) STCW Code A-VI/4"
                );
                break;

            default:
                throw new IllegalArgumentException("지원되지 않는 사고 유형입니다.");
        }

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



    /* 04-02 API 맞춤형 대응 가이드 조회 (Owner 전용, 본인 소유 사건만 가능) */
    @Transactional
    public List<ResponseGuideResponse> getGuides(UUID userId, UUID incidentId) {
        Incident incident = incidentRepository.findById(incidentId)
                .orElseThrow(() -> new IllegalArgumentException("해당 사고를 찾을 수 없습니다."));

        // 본인 소유 사건 검증
        if (!incident.getCreator().getId().equals(userId)) {
            throw new AccessDeniedException("본인 소유 사건의 대응 가이드만 조회할 수 있습니다.");
        }

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

    /* 04-03 API 맞춤형 대응 가이드 삭제 (Owner 전용, 본인 소유 사건만 가능) */
    @Transactional
    public void deleteGuide(UUID userId, UUID incidentId, UUID guideId) {
        Incident incident = incidentRepository.findById(incidentId)
                .orElseThrow(() -> new IllegalArgumentException("해당 사고를 찾을 수 없습니다."));
        
        ResponseGuide guide = responseGuideRepository.findById(guideId)
                .orElseThrow(() -> new IllegalArgumentException("해당 대응 가이드를 찾을 수 없습니다."));
        
        if (!incident.getCreator().getId().equals(userId)) {
            throw new AccessDeniedException("본인 소유 사건의 대응 가이드만 삭제할 수 있습니다.");
        }
                
        
        responseGuideRepository.delete(guide);


    }
}
