package com.andamiro.Dashboard.Dto.EvidenceFileDTO;

import java.time.LocalDateTime;
import java.util.UUID;

// 02-01 목록 조회 응답 DTO
public record EvidenceFileResponse(
        UUID id,
        String fileUrl,
        String fileType,
        String description,
        UUID uploadedBy,
        LocalDateTime createdAt
) {}
