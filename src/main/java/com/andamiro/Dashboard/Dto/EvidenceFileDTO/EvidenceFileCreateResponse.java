package com.andamiro.Dashboard.Dto.EvidenceFileDTO;

import java.time.LocalDateTime;
import java.util.UUID;

// 02-02 업로드 응답 DTO
public record EvidenceFileCreateResponse(
        UUID id,
        UUID incidentId,
        String fileUrl,
        String fileType,
        String description,
        UUID uploaderId,
        LocalDateTime createdAt
) {}
