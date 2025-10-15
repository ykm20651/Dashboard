package com.andamiro.Dashboard.Dto.ReportDTO;

public record FastApiReportResponse(
        String task_id,
        String status,
        String message
) {}
