package com.andamiro.Dashboard.Dto.ReportDTO;

public record FastApiReportRequest(
        String incident_type,
        String description,
        String location,
        String report_type,
        String language
) {}
