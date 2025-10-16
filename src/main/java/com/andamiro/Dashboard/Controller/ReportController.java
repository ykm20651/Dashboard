package com.andamiro.Dashboard.Controller;

import com.andamiro.Dashboard.Dto.ReportDTO.ReportResponse;
import com.andamiro.Dashboard.Security.CustomPrincipal;
import com.andamiro.Dashboard.Service.ReportService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/incidents/{incidentId}/reports")
@RequiredArgsConstructor
@Tag(name = "Report API", description = "사고 보고서 생성 및 조회 API")
public class ReportController {

    private final ReportService reportService;

    /* 03-01 API 보고서 생성 (Owner 전용, 본인 소유 Incident에 한해서) */
    @Operation(summary = "보고서 생성", description = "해당 사고 ID에 대해 보고서를 생성합니다.")
    @PostMapping
    public ResponseEntity<ReportResponse> createReport(
            @AuthenticationPrincipal CustomPrincipal principal,
            @PathVariable UUID incidentId
    ) {
        ReportResponse response = reportService.createReport(principal.getId(), incidentId);
        return ResponseEntity.status(201).body(response);
    }


    /* 03-02 API 보고서 조회 (Owner 전용, 본인 소유 Incident에 한해서) */
    @Operation(summary = "보고서 조회", description = "사고 ID에 해당하는 보고서 목록을 조회합니다.")
    @GetMapping
    public ResponseEntity<List<ReportResponse>> getReports(
            @AuthenticationPrincipal CustomPrincipal principal,
            @PathVariable UUID incidentId
    ) {
        List<ReportResponse> response = reportService.getReports(principal.getId(), incidentId);
        return ResponseEntity.ok(response);
    }

    /*03-03 API 보고서 삭제 */
    @Operation(summary = "보고서 삭제 ", description = "해당 보고서 ID에 대해 생성된 PDF 보고서를 삭제합니다.")
    @DeleteMapping("/{reportId}")
    public ResponseEntity<Void> deleteReport(
            @AuthenticationPrincipal CustomPrincipal principal,
            @PathVariable UUID incidentId,
            @PathVariable UUID reportId) {

        reportService.deleteReport(principal.getId(), incidentId, reportId);
        return ResponseEntity.noContent().build(); // 204 No Content

    }



}
