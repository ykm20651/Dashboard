package com.andamiro.Dashboard.Dto.ReportDTO;

import java.time.LocalDateTime;
import java.util.UUID;

// 보고서 응답 DTO
public record ReportResponse(
        UUID id,
        UUID incidentId,
        String pdfUrl,
        UUID generatedBy,
        LocalDateTime generatedAt
) {}
