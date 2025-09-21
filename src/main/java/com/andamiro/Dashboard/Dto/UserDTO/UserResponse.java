package com.andamiro.Dashboard.Dto.UserDTO;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.UUID;

@Schema(description = "유저 응답 DTO")
public record UserResponse(
        UUID id,
        String email,
        String name,
        String role,
        boolean isApproved
) {}
