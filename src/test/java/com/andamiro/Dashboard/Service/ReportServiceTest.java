package com.andamiro.Dashboard.Service;

import com.andamiro.Dashboard.Config.ReportTestConfig;
import com.andamiro.Dashboard.Dto.ReportDTO.ReportResponse;
import com.andamiro.Dashboard.Entity.Incident;
import com.andamiro.Dashboard.Entity.Report;
import com.andamiro.Dashboard.Entity.User;
import com.andamiro.Dashboard.Fixture.IncidentFixture;
import com.andamiro.Dashboard.Fixture.ReportFixture;
import com.andamiro.Dashboard.Fixture.UserFixture;
import com.andamiro.Dashboard.Repository.IncidentRepository;
import com.andamiro.Dashboard.Repository.ReportRepository;
import com.andamiro.Dashboard.Repository.UserRepository;
import com.andamiro.Dashboard.Util.TestEntityUtil;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockMultipartFile;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@SpringBootTest(classes = {ReportService.class, ReportTestConfig.class})
class ReportServiceTest {

    @Autowired
    private ReportService reportService;

    @Autowired
    private ReportRepository reportRepository;

    @Autowired
    private IncidentRepository incidentRepository;

    @Autowired
    private UserRepository userRepository;

    private User testUser;
    private Incident testIncident;
    private UUID userId;
    private UUID incidentId;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        incidentId = UUID.randomUUID();

        // Fixture 활용
        testUser = UserFixture.createTestUser(userId);
        TestEntityUtil.forceSetId(testUser, "id", userId);

        testIncident = IncidentFixture.oilSpillIncident(testUser);
        TestEntityUtil.forceSetId(testIncident, "id", incidentId);
    }

    @Test
    @DisplayName("보고서 생성 성공 - PDF 파일 업로드")
    void createReport_success() {
        // given
        MockMultipartFile file = new MockMultipartFile(
                "file", "report.pdf", "application/pdf", "dummy".getBytes()
        );

        Report savedReport = ReportFixture.createReport(testIncident, testUser, UUID.randomUUID());

        given(incidentRepository.findById(incidentId)).willReturn(Optional.of(testIncident));
        given(userRepository.findById(userId)).willReturn(Optional.of(testUser));
        given(reportRepository.save(any(Report.class))).willReturn(savedReport);

        // when
        ReportResponse response = reportService.createReport(userId, incidentId, file);

        // then
        assertThat(response.incidentId()).isEqualTo(incidentId);
        assertThat(response.generatedBy()).isEqualTo(userId);
        assertThat(response.pdfUrl()).contains("/files/reports/");
        verify(reportRepository).save(any(Report.class));
    }

    @Test
    @DisplayName("보고서 조회 성공")
    void getReports_success() {
        // given
        Report report = ReportFixture.createReport(testIncident, testUser, UUID.randomUUID());

        given(incidentRepository.findById(incidentId)).willReturn(Optional.of(testIncident));
        given(reportRepository.findAll()).willReturn(List.of(report));

        // when
        List<ReportResponse> responses = reportService.getReports(userId, incidentId);

        // then
        assertThat(responses).hasSize(1);
        assertThat(responses.get(0).incidentId()).isEqualTo(incidentId);
    }

    @Test
    @DisplayName("보고서 조회 실패 - 보고서 없음")
    void getReports_notFound() {
        // given
        given(incidentRepository.findById(incidentId)).willReturn(Optional.of(testIncident));
        given(reportRepository.findAll()).willReturn(List.of());

        // when & then
        assertThatThrownBy(() -> reportService.getReports(userId, incidentId))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessage("해당 사고의 보고서를 찾을 수 없습니다.");
    }

    @Test
    @DisplayName("보고서 조회 실패 - Incident 없음")
    void getReports_noIncident() {
        // given
        given(incidentRepository.findById(incidentId)).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> reportService.getReports(userId, incidentId))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessage("해당 사고를 찾을 수 없습니다.");
    }
}
