package com.andamiro.Dashboard.Controller;

import com.andamiro.Dashboard.Config.IncidentTestConfig;
import com.andamiro.Dashboard.Dto.IncidentDTO.*;
import com.andamiro.Dashboard.Fixture.IncidentFixture;
import com.andamiro.Dashboard.Service.IncidentService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.bind.annotation.PathVariable;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.ArgumentMatchers.eq;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(IncidentController.class)
@Import(IncidentTestConfig.class) /// ← IncidentTestConfig 안의 Bean을 현재 테스트 컨텍스트에 등록
@AutoConfigureMockMvc(addFilters = false)  // 시큐리티 필터 무시 -> 지금 IncidentController 로직 테스트만 하고 싶지, 인증/인가까지 검증하고 싶은 건 아님.
public class IncidentControllerTest {

    @Autowired
    private MockMvc mockMvc;
    //->컨트롤러는 Entity를 다루지 않고 Service/DTO만 다룸.

    //@MockBean
    @Autowired
    private IncidentService incidentService; //@MockBean대신 여기서 바로 주입됨. IncidentTestConfig덕분에

    @Autowired
    private ObjectMapper objectMapper; //JSON <->DTO

    /*
    1. 요청 DTO는 IncidentFixture에서 꺼내 쓰기
    2. 응답 DTO는 테스트 코드 내에 작성하여 가독성 확보하자.
     */

    /* 01-01 API 사고 목록 조회 매핑 */
    @Test
    @DisplayName("GET /incidents?id={userId}") // @ReqeustParam이라 쿼리 파라미터로
    void getIncidentTest() throws Exception{
        //given
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
                new IncidentResponse.CreatorSummary(userId, "홍길동") // 👈 여기서 creator.id 세팅됨
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

        //when - 모키토 given함수 - 연관된 하위계층의 메서드 실행(given 데이터 셋업 넣으면)후 반환값 설정
        given(incidentService.getListIncidents(eq(userId))).willReturn(List.of(incident1, incident2));

        //then

        mockMvc.perform(get("/incidents") //mockMvc는 스프링에서 제공하는 가짜 HTTP 클라이언트로, 컨트롤러 메서드 호출 가능.
                        .param("id", userId.toString())) //쿼리 파라미터 붙이기
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].title").value("유류 유출"))//$[0] - 배열의 첫번째 요소.
                .andExpect(jsonPath("$[1].title").value("화재"))
                .andDo(print());
    }


    /* 01-02 API 사고 등록 매핑 */
    @Test
    @DisplayName("POST /incidents") //@RequeestBody
    void createIncidentTest() throws Exception{
        //given - 데이터 셋업

        /* 컨트롤러 메서드의 요청 DTO 객체 생성*/
        UUID userId = UUID.randomUUID();
        IncidentCreateRequest req = IncidentFixture.createIncidentCreateRequest(userId);

        /* 컨트롤러 메서드의 응답 DTO 객체 생성 후 이걸 반환하도록 하게끔 when절에서 실행할거임.*/
        IncidentResponse response = new IncidentResponse(
                userId,
                "유류 유출",
                "OIL_SPILL",
                "부산항",
                "기관실에서 기름이 유출됨",
                LocalDateTime.now(),
                LocalDateTime.now(),
                "OPEN",
                new IncidentResponse.CreatorSummary(userId, "홍길동") //record의 멤버 record는 자동으로 static으로 취급돼.
        );

        //when - 실행
        given(incidentService.createIncident(eq(req))).willReturn(response);

        //then - 검증

        mockMvc.perform(post("/incidents")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req))) // JSON 직렬화
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.title").value("유류 유출"))
                .andExpect(jsonPath("$.incidentType").value("OIL_SPILL"))
                .andExpect(jsonPath("$.creator.name").value("홍길동"))
                .andDo(print());
    }

    /* 01-03 API 사고 상세 조회 매핑 */
    @Test
    @DisplayName("GET /incidents/{id}") //@PathVariable 형식임.
    void getDetailIncidentTest() throws Exception{

        //given - 요청/응답 DTO 객체 생성

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
                List.of(
                        new IncidentDetailResponse.EvidenceFile(
                                UUID.randomUUID(),
                                "http://example.com/file1.jpg",
                                "image",
                                "현장 사진",
                                userId
                        )
                ),
                List.of(
                        new IncidentDetailResponse.Report(
                                UUID.randomUUID(),
                                "http://example.com/report1.pdf",
                                LocalDateTime.now()
                        )
                )
        );
        //when
        given(incidentService.getDetailIncident(eq(incidentId)))
                .willReturn(response);

        //then
        mockMvc.perform(get("/incidents/{id}", incidentId))
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
    @DisplayName("PUT /incidents/{id}") //@PathVariable + @ReuqestBody
    void updateIncidentTest() throws Exception{

        //given -요청,응답 DTO 객체 생성
        UUID incidentId = UUID.randomUUID();

        IncidentUpdateRequest req = IncidentFixture.createIncidentUpdateRequest();
        IncidentUpdateResponse response = new IncidentUpdateResponse(
                incidentId,
                "수정된 제목",
                "수정된 설명 ~ ~",
                "OIL_SPILL",
                "울산항",
                LocalDateTime.now().minusDays(1), // happenedAt (원래 발생 시각)
                LocalDateTime.now()               // updatedAt (수정 시각)
        );

        //when
        given(incidentService.updateIncident(eq(incidentId), any(IncidentUpdateRequest.class)))
                .willReturn(response);


        //then
        mockMvc.perform(put("/incidents/{id}", incidentId)
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
    @DisplayName("DELETE /incidents/{id}")
    void deleteIncidentTest() throws Exception{
        UUID incidentId = UUID.randomUUID();

        mockMvc.perform(delete("/incidents/{id}", incidentId))
                .andExpect(status().isNoContent())
                .andDo(print());
    }

}
