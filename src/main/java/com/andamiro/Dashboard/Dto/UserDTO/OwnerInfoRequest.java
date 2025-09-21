package com.andamiro.Dashboard.Dto.UserDTO;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "선주 추가 정보 요청 DTO")
public record OwnerInfoRequest(
        @Schema(description = "회사명", example = "안다미로해운")
        String companyName,

        @Schema(description = "선박 등록 ID", example = "KR-12345")
        String shipRegId,

        @Schema(description = "연락처", example = "010-1234-5678")
        String contactNumber,

        @Schema(description = "사업자등록번호", example = "123-45-67890")
        String businessNumber
) {}
