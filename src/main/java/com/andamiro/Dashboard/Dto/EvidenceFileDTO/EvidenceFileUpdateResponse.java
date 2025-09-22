package com.andamiro.Dashboard.Dto.EvidenceFileDTO;

import java.time.LocalDateTime;
import java.util.UUID;

// 02-04 수정 응답 DTO
public record EvidenceFileUpdateResponse(
        UUID id,
        UUID incidentId,
        String fileUrl,
        String fileType,
        String description,
        LocalDateTime updatedAt
) {}