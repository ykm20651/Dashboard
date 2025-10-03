package com.andamiro.Dashboard.Controller;

import com.andamiro.Dashboard.Dto.ReportDTO.ReportResponse;
import com.andamiro.Dashboard.Service.ReportService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/incidents/{incidentId}/reports")
@RequiredArgsConstructor
@Tag(name = "Report API", description = "사고 보고서 생성 및 조회 API")
public class ReportController {

    private final ReportService reportService;

    /* 03-01 API 보고서 생성 (Owner 전용, 본인 소유 Incident에 한해서) */
    @Operation(summary = "보고서 생성", description = "해당 사고 ID에 대해 보고서를 생성합니다. PDF 파일 업로드 방식.")
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ReportResponse> createReport(
            @AuthenticationPrincipal UUID userId,        // JWT 인증된 사용자
            @PathVariable UUID incidentId,
            @RequestPart("file") MultipartFile file
    ) {
        ReportResponse response = reportService.createReport(userId, incidentId, file);
        return ResponseEntity.status(201).body(response);
    }

    /* 03-02 API 보고서 조회 (Owner 전용, 본인 소유 Incident에 한해서) */
    @Operation(summary = "보고서 조회", description = "사고 ID에 해당하는 보고서 목록을 조회합니다.")
    @GetMapping
    public ResponseEntity<List<ReportResponse>> getReports(
            @AuthenticationPrincipal UUID userId,
            @PathVariable UUID incidentId
    ) {
        List<ReportResponse> response = reportService.getReports(userId, incidentId);
        return ResponseEntity.ok(response);
    }
}
