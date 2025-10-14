package com.andamiro.Dashboard.Security;

import java.util.UUID;

/* Spring Security 인증 컨텍스트에 저장되는 사용자 정보 객체 (JWT 토큰 해석 결과를 담는 Principal) */
public class CustomPrincipal {

    private final UUID id;
    private final String role;

    public CustomPrincipal(UUID id, String role) {
        this.id = id;
        this.role = role;
    }

    public UUID getId() {
        return id;
    }

    public String getRole() {
        return role;
    }

    @Override
    public String toString() {
        return "CustomPrincipal{id=" + id + ", role='" + role + "'}";
    }
}
