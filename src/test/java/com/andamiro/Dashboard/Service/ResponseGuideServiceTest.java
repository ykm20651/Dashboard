package com.andamiro.Dashboard.Service;

import com.andamiro.Dashboard.Config.ReportTestConfig; // ReportTestConfig 재활용
import com.andamiro.Dashboard.Config.ResponseGuideTestConfig;
import com.andamiro.Dashboard.Dto.ResponseGuideDTO.*;
import com.andamiro.Dashboard.Entity.Incident;
import com.andamiro.Dashboard.Entity.ResponseGuide;
import com.andamiro.Dashboard.Entity.User;
import com.andamiro.Dashboard.Fixture.IncidentFixture;
import com.andamiro.Dashboard.Fixture.UserFixture;
import com.andamiro.Dashboard.Repository.IncidentRepository;
import com.andamiro.Dashboard.Repository.ResponseGuideRepository;
import com.andamiro.Dashboard.Repository.UserRepository;
import com.andamiro.Dashboard.Util.TestEntityUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.access.AccessDeniedException;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@SpringBootTest(classes = {ResponseGuideService.class, ResponseGuideTestConfig.class})
class ResponseGuideServiceTest {

    @Autowired private ResponseGuideService responseGuideService;
    @Autowired private ResponseGuideRepository responseGuideRepository;
    @Autowired private IncidentRepository incidentRepository;
    @Autowired private UserRepository userRepository;

    private User testUser;
    private Incident testIncident;
    private UUID userId;
    private UUID incidentId;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        incidentId = UUID.randomUUID();

        testUser = UserFixture.createTestUser(userId);
        TestEntityUtil.forceSetId(testUser, "id", userId);

        testIncident = IncidentFixture.oilSpillIncident(testUser);
        TestEntityUtil.forceSetId(testIncident, "id", incidentId);
    }

    /* 04-01 생성 */
    @Test
    @DisplayName("대응 가이드 생성 성공 - 본인 소유 사건")
    void createGuide_success() {
        // given
        ResponseGuide saved = ResponseGuide.create(
                testIncident.getIncidentType(),
                "유류 유출 초기 대응 지침",
                "즉각 해상 차단막 설치",
                "차단막 설치,환경청 신고,기름 회수 장비 투입",
                "해양환경관리법 제12조"
        );
        TestEntityUtil.forceSetId(saved, "id", UUID.randomUUID());

        given(incidentRepository.findById(incidentId)).willReturn(Optional.of(testIncident));
        given(responseGuideRepository.save(any(ResponseGuide.class))).willReturn(saved);

        // when
        ResponseGuideCreateResponse response = responseGuideService.createGuide(userId, incidentId);

        // then
        assertThat(response.incidentId()).isEqualTo(incidentId);
        assertThat(response.guides().get(0).title()).isEqualTo("유류 유출 초기 대응 지침");
        verify(responseGuideRepository).save(any(ResponseGuide.class));
    }

    @Test
    @DisplayName("대응 가이드 생성 실패 - 소유자가 아님")
    void createGuide_accessDenied() {
        // given
        User otherUser = UserFixture.createTestUser(UUID.randomUUID());
        Incident otherIncident = IncidentFixture.oilSpillIncident(otherUser);
        TestEntityUtil.forceSetId(otherIncident, "id", incidentId);

        given(incidentRepository.findById(incidentId)).willReturn(Optional.of(otherIncident));

        // when & then
        assertThatThrownBy(() -> responseGuideService.createGuide(userId, incidentId))
                .isInstanceOf(AccessDeniedException.class)
                .hasMessage("본인 소유 사건에 대해서만 대응 가이드를 생성할 수 있습니다.");
    }

    /* 04-02 조회 */
    @Test
    @DisplayName("대응 가이드 조회 성공 - 본인 소유 사건")
    void getGuides_success() {
        // given
        ResponseGuide guide = ResponseGuide.create(
                testIncident.getIncidentType(),
                "유류 유출 초기 대응 지침",
                "즉각 해상 차단막 설치",
                "차단막 설치,환경청 신고,기름 회수 장비 투입",
                "해양환경관리법 제12조"
        );
        TestEntityUtil.forceSetId(guide, "id", UUID.randomUUID());

        given(incidentRepository.findById(incidentId)).willReturn(Optional.of(testIncident));
        given(responseGuideRepository.findAll()).willReturn(List.of(guide));

        // when
        List<ResponseGuideResponse> responses = responseGuideService.getGuides(userId, incidentId);

        // then
        assertThat(responses).hasSize(1);
        assertThat(responses.get(0).title()).isEqualTo("유류 유출 초기 대응 지침");
    }

    @Test
    @DisplayName("대응 가이드 조회 실패 - 사건 없음")
    void getGuides_notFoundIncident() {
        // given
        given(incidentRepository.findById(incidentId)).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> responseGuideService.getGuides(userId, incidentId))
                .isInstanceOf(IllegalArgumentException.class) // 바뀐 예외에 맞춤
                .hasMessage("해당 사고를 찾을 수 없습니다.");
    }

    @Test
    @DisplayName("대응 가이드 조회 실패 - 소유자가 아님")
    void getGuides_accessDenied() {
        // given
        User otherUser = UserFixture.createTestUser(UUID.randomUUID());
        Incident otherIncident = IncidentFixture.oilSpillIncident(otherUser);
        TestEntityUtil.forceSetId(otherIncident, "id", incidentId);

        given(incidentRepository.findById(incidentId)).willReturn(Optional.of(otherIncident));

        // when & then
        assertThatThrownBy(() -> responseGuideService.getGuides(userId, incidentId))
                .isInstanceOf(AccessDeniedException.class)
                .hasMessage("본인 소유 사건의 대응 가이드만 조회할 수 있습니다.");
    }
}
