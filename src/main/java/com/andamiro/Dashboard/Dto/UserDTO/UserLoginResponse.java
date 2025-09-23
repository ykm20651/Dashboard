package com.andamiro.Dashboard.Dto.UserDTO;

import com.andamiro.Dashboard.Entity.User;

public record UserLoginResponse(
        String token,
        String tokenType, // "Bearer"
        String email,
        User.Role role
) {}
