package com.andamiro.Dashboard.Controller;

import com.andamiro.Dashboard.Config.IncidentTestConfig;
import com.andamiro.Dashboard.Dto.IncidentDTO.*;
import com.andamiro.Dashboard.Fixture.IncidentFixture;
import com.andamiro.Dashboard.Service.IncidentService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willDoNothing;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(IncidentController.class) //Controller 레이어만 올리고, Service/Repository 같은 Bean은 전혀 올리지 않아.
@AutoConfigureMockMvc(addFilters = false) // 시큐리티 필터 무시
@Import(IncidentTestConfig.class)
public class IncidentControllerTest {

    @Autowired private MockMvc mockMvc;

    @MockitoBean private IncidentService incidentService; // IncidentTestConfig 덕분에 Mock 주입
    @Autowired private ObjectMapper objectMapper;

    /*
     1. 요청 DTO는 Fixture에서 꺼내 사용
     2. 응답 DTO는 테스트 메서드 내에서 직접 생성 (가독성 ↑)
     */



    /* 01-01 API 사고 목록 조회 매핑 */
    @Test
    @DisplayName("01-01 GET /incidents")
    void getIncidentTest() throws Exception {
        UUID userId = UUID.randomUUID();

        IncidentResponse incident1 = new IncidentResponse(
                UUID.randomUUID(),
                "유류 유출",
                "oil_spill",
                "부산항",
                "기관실에서 기름이 유출됨",
                LocalDateTime.now(),
                LocalDateTime.now(),
                "OPEN",
                new IncidentResponse.CreatorSummary(userId, "홍길동")
        );

        IncidentResponse incident2 = new IncidentResponse(
                UUID.randomUUID(),
                "화재",
                "fire",
                "울산항",
                "기관실 화재 발생",
                LocalDateTime.now(),
                LocalDateTime.now(),
                "OPEN",
                new IncidentResponse.CreatorSummary(userId, "홍길동")
        );

        // Service Mocking
        given(incidentService.getListIncidents(Mockito.<UUID>any()))
                .willReturn(List.of(incident1, incident2));

        mockMvc.perform(get("/incidents")
                        .header("Authorization", "Bearer faketoken")) // AuthenticationPrincipal 흉내
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].title").value("유류 유출"))
                .andExpect(jsonPath("$[1].title").value("화재"))
                .andDo(print());
    }

    /* 01-02 API 사고 등록 매핑 */
    @Test
    @DisplayName("01-02 POST /incidents")
    void createIncidentTest() throws Exception {
        UUID userId = UUID.randomUUID();
        IncidentCreateRequest req = IncidentFixture.createIncidentCreateRequest();

        IncidentResponse response = new IncidentResponse(
                UUID.randomUUID(),
                "유류 유출",
                "OIL_SPILL",
                "부산항",
                "기관실에서 기름이 유출됨",
                LocalDateTime.now(),
                LocalDateTime.now(),
                "OPEN",
                new IncidentResponse.CreatorSummary(userId, "홍길동")
        );

        //given(incidentService.createIncident(any(UUID.class), any(IncidentCreateRequest.class)))
        //        .willReturn(response);
        given(incidentService.createIncident(Mockito.<UUID>any(), Mockito.any(IncidentCreateRequest.class)))
                .willReturn(response);


        mockMvc.perform(post("/incidents")
                        .header("Authorization", "Bearer faketoken")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.title").value("유류 유출"))
                .andExpect(jsonPath("$.incidentType").value("OIL_SPILL"))
                .andExpect(jsonPath("$.creator.name").value("홍길동"))
                .andDo(print());
    }

    /* 01-03 API 사고 상세 조회 매핑 */
    @Test
    @DisplayName("01-03 GET /incidents/{id}")
    void getDetailIncidentTest() throws Exception {
        UUID incidentId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();

        IncidentDetailResponse response = new IncidentDetailResponse(
                incidentId,
                "유류 유출",
                "기관실에서 기름이 유출됨",
                "oil_spill",
                "부산항",
                LocalDateTime.now(),
                LocalDateTime.now(),
                "OPEN",
                new IncidentDetailResponse.Creator(userId, "홍길동"),
                List.of(new IncidentDetailResponse.EvidenceFile(
                        UUID.randomUUID(),
                        "http://example.com/file1.jpg",
                        "image",
                        "현장 사진",
                        userId
                )),
                List.of(new IncidentDetailResponse.Report(
                        UUID.randomUUID(),
                        "http://example.com/report1.pdf",
                        LocalDateTime.now()
                ))
        );

        given(incidentService.getDetailIncident(Mockito.<UUID>any(), Mockito.<UUID>any()))
                .willReturn(response);

        mockMvc.perform(get("/incidents/{id}", incidentId)
                        .header("Authorization", "Bearer faketoken"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(incidentId.toString()))
                .andExpect(jsonPath("$.title").value("유류 유출"))
                .andExpect(jsonPath("$.creator.name").value("홍길동"))
                .andExpect(jsonPath("$.evidenceFiles[0].fileUrl").value("http://example.com/file1.jpg"))
                .andExpect(jsonPath("$.reports[0].pdfUrl").value("http://example.com/report1.pdf"))
                .andDo(print());
    }

    /* 01-04 API 사고 수정 매핑 */
    @Test
    @DisplayName("01-04 PUT /incidents/{id}")
    void updateIncidentTest() throws Exception {
        UUID userId = UUID.randomUUID();
        UUID incidentId = UUID.randomUUID();

        IncidentUpdateRequest req = IncidentFixture.createIncidentUpdateRequest();

        IncidentUpdateResponse response = new IncidentUpdateResponse(
                incidentId,
                "수정된 제목",
                "수정된 설명 ~ ~",
                "OIL_SPILL",
                "울산항",
                LocalDateTime.now().minusDays(1),
                LocalDateTime.now()
        );

        given(incidentService.updateIncident(Mockito.<UUID>any(), Mockito.<UUID>any(), Mockito.<IncidentUpdateRequest>any()))
                .willReturn(response);

        mockMvc.perform(put("/incidents/{id}", incidentId)
                        .header("Authorization", "Bearer faketoken")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(incidentId.toString()))
                .andExpect(jsonPath("$.title").value("수정된 제목"))
                .andExpect(jsonPath("$.description").value("수정된 설명 ~ ~"))
                .andExpect(jsonPath("$.location").value("울산항"))
                .andExpect(jsonPath("$.incidentType").value("OIL_SPILL"))
                .andDo(print());
    }

    /* 01-05 API 사고 삭제 매핑 */
    @Test
    @DisplayName("01-05 DELETE /incidents/{id}")
    void deleteIncidentTest() throws Exception {
        UUID incidentId = UUID.randomUUID();

        willDoNothing().given(incidentService).deleteIncident(any(UUID.class), any(UUID.class));

        mockMvc.perform(delete("/incidents/{id}", incidentId)
                        .header("Authorization", "Bearer faketoken"))
                .andExpect(status().isNoContent())
                .andDo(print());
    }
}
