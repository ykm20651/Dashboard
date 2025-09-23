package com.andamiro.Dashboard.Dto.IncidentDTO;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public record IncidentDetailResponse(
        UUID id,
        String title,
        String description,
        String incidentType,
        String location,
        LocalDateTime happenedAt,
        LocalDateTime reportedAt,
        String status,
        Creator creator,
        List<EvidenceFile> evidenceFiles,
        List<Report> reports
) {
    public record Creator(UUID id, String name) {}
    public record EvidenceFile(UUID id, String fileUrl, String fileType, String description, UUID uploadedBy) {}
    public record Report(UUID id, String pdfUrl, LocalDateTime generatedAt) {}
}
