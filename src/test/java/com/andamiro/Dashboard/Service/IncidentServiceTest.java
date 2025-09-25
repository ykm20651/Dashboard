package com.andamiro.Dashboard.Service;

import com.andamiro.Dashboard.Config.IncidentTestConfig;
import com.andamiro.Dashboard.Dto.IncidentDTO.IncidentCreateRequest;
import com.andamiro.Dashboard.Dto.IncidentDTO.IncidentResponse;
import com.andamiro.Dashboard.Dto.IncidentDTO.IncidentUpdateRequest;
import com.andamiro.Dashboard.Dto.IncidentDTO.IncidentUpdateResponse;
import com.andamiro.Dashboard.Entity.Incident;
import com.andamiro.Dashboard.Entity.User;
import com.andamiro.Dashboard.Repository.IncidentRepository;
import com.andamiro.Dashboard.Repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.assertj.core.api.BDDAssertions.then; //값 검증


//프로젝트 전체 Bean을 다 올리는데, 실서비스 환경과 똑같이 동작함. 다만 너무 무거워서 아래와같은 옵션을 줘서 올릴 Bean을 제한하였음.
//classes={(테스트할 대상), (Mock Repository를 Bean으로 등록해주는 설정)}
@SpringBootTest(classes = {IncidentService.class, IncidentTestConfig.class})
public class IncidentServiceTest {
    //테스트할 Service + 필요한 Mock Bean만 최소한으로 ApllicationContext에 띄운다.
    @Autowired
    private IncidentService incidentService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private IncidentRepository incidentRepository; // mock 객체 (@Primary 적용된 Bean)

    /*
    IncidentService의 5개 메서드를 전부 테스트하지 않음.
    단순 Repository CRUD 위임부분은 JPA의 책임이므로 중복 검증 x
    서비스 로직이 추가된 부분(create, update, delete, 예외처리)은 단위 테스트로 집중해서 검증함.

    -> 이렇게 하면 테스트 코드가 단순 검증에 매몰되지 않고, 실제 비즈니스 로직에 대한 신뢰성을 확보할 수 있을 것 같았음.
     */

    /* 01-02 API 사고 등록 매핑 */
    @Test
    @DisplayName("createIncident 메서드는 유효한 요청이 들어오면 사고를 성공적으로 저장한다")
    void createIncidentTest(){
        // given
        UUID userId = UUID.randomUUID();
        IncidentCreateRequest request = new IncidentCreateRequest(
                userId,
                "유류 유출",
                "기관실에서 기름이 유출됨",
                "OIL_SPILL",
                "부산항",
                LocalDateTime.now()
        );

        // 가짜 사용자
        User fakeUser = User.create("ykm3065@example.com", "password1234", "유경민", User.Role.OWNER);

        // 가짜 Incident
        Incident incident = Incident.create(
                fakeUser,
                "유류 유출",
                "기관실에서 기름이 유출됨",
                Incident.IncidentType.OIL_SPILL,
                "부산항",
                LocalDateTime.now()
        );

        // ★ UserRepository에서 userId 조회하면 fakeUser 반환하도록 세팅
        given(userRepository.findById(userId)).willReturn(Optional.of(fakeUser));

        // IncidentRepository.save 호출하면 incident 반환
        given(incidentRepository.save(any(Incident.class))).willReturn(incident);

        // when
        IncidentResponse result = incidentService.createIncident(request);

        // then - DTO가 record로 선언되어있어 필드명 그대로 getter만들어져있음.
        then(result.title()).isEqualTo("유류 유출");
        then(result.incidentType()).isEqualTo("oil_spill");
        then(result.location()).isEqualTo("부산항");
    }

    /* 01-04 API 사고 수정 매핑 */
    @Test
    @DisplayName("updateIncident 메서드는 특정 사고 정보에 대하여 수정을 성공적 완료")
    void updateIncidentTest(){
        //given
        UUID id = UUID.randomUUID();
        IncidentUpdateRequest request = new IncidentUpdateRequest(
                "수정된 제목",
                "수정된 설명",
                "울산항"
        );

        // 가짜 사용자
        User fakeUser = User.create("ykm3065@example.com", "password1234", "유경민", User.Role.OWNER);

        // 가짜 Incident
        Incident incident = Incident.create(
                fakeUser,
                "유류 유출",
                "기관실에서 기름이 유출됨",
                Incident.IncidentType.OIL_SPILL,
                "부산항",
                LocalDateTime.now()
        );

        // Incident가 DB에 있는 것처럼 findById가 incident 반환하도록 Mocking
        given(incidentRepository.findById(id)).willReturn(Optional.of(incident));
        given(incidentRepository.save(any(Incident.class))).willReturn(incident);
        //when
        IncidentUpdateResponse result = incidentService.updateIncident(id, request);

        //then
        then(result.title()).isEqualTo("수정된 제목");
        then(result.description()).isEqualTo("수정된 설명");
        then(result.location()).isEqualTo("울산항");
        then(result.incidentType()).isEqualTo("oil_spill"); // 타입은 그대로


    }



}
