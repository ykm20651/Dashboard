package com.andamiro.Dashboard.Dto.ResponseGuideDTO;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

// 04-02 응답 DTO
public record ResponseGuideResponse(
        UUID id,
        String incidentType,
        String title,
        String description,
        List<String> checklist,
        String legalClause,
        LocalDateTime createdAt
) {}
