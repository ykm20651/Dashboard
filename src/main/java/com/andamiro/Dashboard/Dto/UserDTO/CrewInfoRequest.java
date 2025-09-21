package com.andamiro.Dashboard.Dto.UserDTO;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "선원 추가 정보 요청 DTO")
public record CrewInfoRequest(
        @Schema(description = "소속 선주 ID", example = "uuid-owner-1")
        String assignedOwnerId,

        @Schema(description = "직책", example = "기관사")
        String position
) {}
