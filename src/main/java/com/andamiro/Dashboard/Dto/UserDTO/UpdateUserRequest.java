package com.andamiro.Dashboard.Dto.UserDTO;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "유저 정보 수정 요청 DTO")
public record UpdateUserRequest(
        @Schema(description = "이름", example = "홍길동2")
        String name,

        @Schema(description = "비밀번호", example = "newpassword123")
        String password
) {}
