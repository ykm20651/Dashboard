package com.andamiro.Dashboard.Controller;

import com.andamiro.Dashboard.Dto.ReportDTO.ReportResponse;
import com.andamiro.Dashboard.Service.ReportService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;
import java.util.List;

@RestController
@RequestMapping("/incidents/{id}/reports")
@RequiredArgsConstructor
@Tag(name = "Report API", description = "사고 보고서 생성 및 조회 API")
public class ReportController {

    private final ReportService reportService;

    /* 03-01 API 보고서 생성 매핑 (파일 업로드 방식)*/
    @Operation(summary = "보고서 생성", description = "사고 ID에 해당하는 보고서를 생성합니다. PDF 파일 업로드 방식.")
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ReportResponse> createReport(
            @PathVariable UUID id,
            @RequestParam UUID userId,  // TODO: JWT 인증 적용 후 SecurityContext에서 가져오기
            @RequestPart("file") MultipartFile file
    ) {
        ReportResponse response = reportService.createReport(id, userId, file);
        return ResponseEntity.status(201).body(response);
    }

    /* 03-02 API 보고서 조회 매핑 */
    @Operation(summary = "보고서 조회", description = "사고 ID에 해당하는 보고서를 조회합니다.")
    @GetMapping
    public ResponseEntity<List<ReportResponse>> getReport(@PathVariable UUID id) {
        List<ReportResponse> response = reportService.getReports(id);
        return ResponseEntity.ok(response);
    }
}
