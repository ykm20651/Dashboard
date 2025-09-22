package com.andamiro.Dashboard.Dto.ResponseGuideDTO;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

// 04-01 응답 DTO
public record ResponseGuideCreateResponse(
        UUID incidentId,
        List<Guide> guides,
        LocalDateTime createdAt
) {
    public record Guide(
            UUID id,
            String incidentType,
            String title,
            String description,
            List<String> checklist,
            String legalClause
    ) {}
}
