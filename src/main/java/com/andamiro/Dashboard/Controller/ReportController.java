package com.andamiro.Dashboard.Controller;

import com.andamiro.Dashboard.Dto.ReportDTO.ReportResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.UUID;

@RestController
@RequestMapping("/incidents/{id}/report")
@Tag(name = "Report API", description = "사고 보고서 생성 및 조회 API")
public class ReportController {

    /* 03-01: 보고서 생성 */
    @Operation(summary = "보고서 생성", description = "사고 ID에 해당하는 보고서를 생성합니다.")
    @PostMapping
    public ResponseEntity<ReportResponse> createReport(@PathVariable UUID id) {
        // TODO: service.createReport(incidentId)
        ReportResponse response = new ReportResponse(
                UUID.randomUUID(),
                id,
                "https://cdn.service.com/report1.pdf",
                UUID.randomUUID(),   // generatedBy (예: 사용자 ID)
                LocalDateTime.now()
        );
        return ResponseEntity.status(201).body(response);
    }

    /* 03-02: 보고서 조회 */
    @Operation(summary = "보고서 조회", description = "사고 ID에 해당하는 보고서를 조회합니다.")
    @GetMapping
    public ResponseEntity<ReportResponse> getReport(@PathVariable UUID id) {
        // TODO: service.getReport(incidentId)
        ReportResponse response = new ReportResponse(
                UUID.fromString("11111111-1111-1111-1111-111111111111"),
                id,
                "https://cdn.service.com/report1.pdf",
                UUID.fromString("22222222-2222-2222-2222-222222222222"),
                LocalDateTime.parse("2025-09-20T12:00:00")
        );
        return ResponseEntity.ok(response);
    }
}
