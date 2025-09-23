package com.andamiro.Dashboard.Dto.UserDTO;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import com.andamiro.Dashboard.Entity.User;
import jakarta.validation.constraints.NotNull;

@Schema(description = "공통 회원가입 요청 DTO")
public record UserSignupRequest(
        @Email @NotBlank
        @Schema(description = "이메일", example = "user1@ship.com")
        String email,

        @NotBlank //문자열 전용 어노테이션
        @Schema(description = "비밀번호", example = "123456")
        String password,

        @NotBlank
        @Schema(description = "이름", example = "홍길동")
        String name,

        @NotNull
        @Schema(description = "역할 (OWNER=선주, CREW=선원, ADMIN=관리자)", example = "OWNER")
        User.Role role //User 클래스 내부에 enum이 static 클래스로 선언되어있어 '.'으로 들어갈 수 있음.

) {}
