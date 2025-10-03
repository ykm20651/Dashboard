package com.andamiro.Dashboard.Controller;

import com.andamiro.Dashboard.Dto.ReportDTO.ReportResponse;
import com.andamiro.Dashboard.Service.ReportService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ReportController.class)
@AutoConfigureMockMvc(addFilters = false) // 🔥 Security 필터 비활성화
class ReportControllerTest {

    @Autowired private MockMvc mockMvc;
    @MockitoBean private ReportService reportService;
    @Autowired private ObjectMapper objectMapper;

    /* 03-01 보고서 생성 */
    @Test
    @DisplayName("03-01 보고서 생성 API - PDF 업로드")
    void createReport() throws Exception {
        UUID incidentId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();

        MockMultipartFile file = new MockMultipartFile(
                "file", "test.pdf", "application/pdf", "dummy content".getBytes()
        );

        ReportResponse resp = new ReportResponse(
                UUID.randomUUID(),
                incidentId,
                "/files/reports/test.pdf",
                userId,
                java.time.LocalDateTime.now()
        );

        given(reportService.createReport(Mockito.<UUID>any(), Mockito.<UUID>any(), Mockito.<MultipartFile>any())).willReturn(resp);

        mockMvc.perform(multipart("/incidents/" + incidentId + "/reports")
                        .file(file)
                        .param("userId", userId.toString()) // @RequestParam
                        .contentType(MediaType.MULTIPART_FORM_DATA))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.incidentId").value(incidentId.toString()))
                .andExpect(jsonPath("$.pdfUrl").value("/files/reports/test.pdf"));
    }

    /* 03-02 보고서 조회 */
    @Test
    @DisplayName("03-02 보고서 조회 API")
    void getReports() throws Exception {
        UUID incidentId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();

        ReportResponse resp = new ReportResponse(
                UUID.randomUUID(),
                incidentId,
                "/files/reports/test.pdf",
                userId,
                java.time.LocalDateTime.now()
        );

        given(reportService.getReports(Mockito.<UUID>any(),Mockito.<UUID>any())).willReturn(List.of(resp));

        mockMvc.perform(get("/incidents/" + incidentId + "/reports"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].incidentId").value(incidentId.toString()))
                .andExpect(jsonPath("$[0].pdfUrl").value("/files/reports/test.pdf"));
    }
}
