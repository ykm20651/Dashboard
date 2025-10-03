package com.andamiro.Dashboard.Controller;

import com.andamiro.Dashboard.Dto.ResponseGuideDTO.*;
import com.andamiro.Dashboard.Service.ResponseGuideService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ResponseGuideController.class)
@AutoConfigureMockMvc(addFilters = false) // 🔥 보안 필터 끔
class ResponseGuideControllerTest {

    @Autowired private MockMvc mockMvc;
    @MockitoBean private ResponseGuideService responseGuideService;
    @Autowired private ObjectMapper objectMapper;

    /* 04-01 생성 */
    @Test
    @DisplayName("04-01 대응 가이드 생성 API")
    void createGuide() throws Exception {
        UUID incidentId = UUID.randomUUID();
        UUID guideId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();

        ResponseGuideCreateResponse resp = new ResponseGuideCreateResponse(
                incidentId,
                List.of(new ResponseGuideCreateResponse.Guide(
                        guideId, "OIL_SPILL", "유류 유출 초기 대응 지침",
                        "즉각 해상 차단막 설치",
                        List.of("차단막 설치", "환경청 신고", "기름 회수 장비 투입"),
                        "해양환경관리법 제12조"
                )),
                LocalDateTime.now()
        );

        given(responseGuideService.createGuide(Mockito.<UUID>any(), Mockito.<UUID>any()))
                .willReturn(resp);

        mockMvc.perform(post("/incidents/" + incidentId + "/response-guide")
                        .principal(() -> userId.toString()))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.incidentId").value(incidentId.toString()))
                .andExpect(jsonPath("$.guides[0].title").value("유류 유출 초기 대응 지침"));
    }

    /* 04-02 조회 */
    @Test
    @DisplayName("04-02 대응 가이드 조회 API")
    void getGuides() throws Exception {
        UUID incidentId = UUID.randomUUID();
        UUID guideId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();

        ResponseGuideResponse resp = new ResponseGuideResponse(
                guideId, "OIL_SPILL", "유류 유출 초기 대응 지침",
                "즉각 해상 차단막 설치",
                List.of("차단막 설치", "환경청 신고", "기름 회수 장비 투입"),
                "해양환경관리법 제12조",
                LocalDateTime.now()
        );

        given(responseGuideService.getGuides(Mockito.<UUID>any(), Mockito.<UUID>any()))
                .willReturn(List.of(resp));

        mockMvc.perform(get("/incidents/" + incidentId + "/response-guide")
                        .principal(() -> userId.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(guideId.toString()))
                .andExpect(jsonPath("$[0].title").value("유류 유출 초기 대응 지침"));
    }
}
