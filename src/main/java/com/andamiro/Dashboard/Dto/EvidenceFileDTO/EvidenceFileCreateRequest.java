package com.andamiro.Dashboard.Dto.EvidenceFileDTO;

// 02-02 업로드 요청 DTO
public record EvidenceFileCreateRequest(
        String fileUrl,
        String fileType,
        String description
) {}
