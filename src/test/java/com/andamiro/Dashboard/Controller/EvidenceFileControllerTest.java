package com.andamiro.Dashboard.Controller;

import com.andamiro.Dashboard.Dto.EvidenceFileDTO.*;
import com.andamiro.Dashboard.Service.EvidenceFileService;
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

import java.util.List;
import java.util.UUID;

import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(EvidenceFileController.class)
@AutoConfigureMockMvc(addFilters = false) // 🔥 보안 필터 끔
class EvidenceFileControllerTest {

    @Autowired private MockMvc mockMvc;
    @MockitoBean private EvidenceFileService evidenceFileService;
    @Autowired private ObjectMapper objectMapper;

    /* 02-01 목록 조회 */
    @Test
    @DisplayName("02-01 증거자료 목록 조회 API")
    void getEvidenceFiles() throws Exception {
        UUID incidentId = UUID.randomUUID();
        EvidenceFileResponse resp = new EvidenceFileResponse(
                UUID.randomUUID(), incidentId, "http://test.jpg", "image", "desc", UUID.randomUUID(), java.time.LocalDateTime.now()
        );

        given(evidenceFileService.getEvidenceFiles(incidentId)).willReturn(List.of(resp));

        mockMvc.perform(get("/incidents/" + incidentId + "/evidence-files"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].fileUrl").value("http://test.jpg"));
    }

    /* 02-02 업로드 */
    @Test
    @DisplayName("02-02 증거자료 업로드 API")
    void uploadEvidenceFile() throws Exception {
        UUID incidentId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();

        EvidenceFileCreateRequest req = new EvidenceFileCreateRequest("http://test.jpg", "IMAGE", "desc");
        EvidenceFileCreateResponse resp = new EvidenceFileCreateResponse(
                UUID.randomUUID(), incidentId, "http://test.jpg", "image", "desc", userId, java.time.LocalDateTime.now()
        );

        given(evidenceFileService.uploadEvidenceFile(Mockito.<UUID>any(), Mockito.<UUID>any(), Mockito.any(EvidenceFileCreateRequest.class)))
                .willReturn(resp);

        mockMvc.perform(post("/incidents/" + incidentId + "/evidence-files")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req))
                        .principal(() -> userId.toString()))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.incidentId").value(incidentId.toString()));
    }

    /* 02-03 삭제 */
    @Test
    @DisplayName("02-03 증거자료 삭제 API")
    void deleteEvidenceFile() throws Exception {
        UUID fileId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();

        Mockito.doNothing().when(evidenceFileService).deleteEvidenceFile(userId, fileId);

        mockMvc.perform(delete("/evidence-files/" + fileId)
                        .principal(() -> userId.toString()))
                .andExpect(status().isNoContent());
    }

    /* 02-04 수정 */
    @Test
    @DisplayName("02-04 증거자료 수정 API")
    void updateEvidenceFile() throws Exception {
        UUID fileId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();

        EvidenceFileUpdateRequest req = new EvidenceFileUpdateRequest("new desc");
        EvidenceFileUpdateResponse resp = new EvidenceFileUpdateResponse(
                fileId, UUID.randomUUID(), "http://test.jpg", "image", "new desc", java.time.LocalDateTime.now()
        );

        given(evidenceFileService.updateEvidenceFile(Mockito.<UUID>any(), Mockito.<UUID>any(), Mockito.any(EvidenceFileUpdateRequest.class)))
                .willReturn(resp);

        mockMvc.perform(put("/evidence-files/" + fileId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req))
                        .principal(() -> userId.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.description").value("new desc"));
    }
}
