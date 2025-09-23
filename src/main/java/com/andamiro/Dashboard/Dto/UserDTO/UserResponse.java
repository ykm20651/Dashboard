package com.andamiro.Dashboard.Dto.UserDTO;

import com.andamiro.Dashboard.Entity.User;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.UUID;

@Schema(description = "유저 응답 DTO")
public record UserResponse(
        UUID id,
        String email,
        String name,
        User.Role role,
        boolean isApproved
) {}
