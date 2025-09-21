package com.andamiro.Dashboard.Dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "승인 요청 DTO")
public record ApproveRequest(
        @Schema(description = "승인 여부", example = "true")
        boolean approved
) {}
