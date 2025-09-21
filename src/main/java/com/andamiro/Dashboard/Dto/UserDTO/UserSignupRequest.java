package com.andamiro.Dashboard.Dto.UserDTO;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "공통 회원가입 요청 DTO")
public record UserSignupRequest(
        @Email @NotBlank
        @Schema(description = "이메일", example = "user1@ship.com")
        String email,

        @NotBlank
        @Schema(description = "비밀번호", example = "123456")
        String password,

        @NotBlank
        @Schema(description = "이름", example = "홍길동")
        String name
) {}
