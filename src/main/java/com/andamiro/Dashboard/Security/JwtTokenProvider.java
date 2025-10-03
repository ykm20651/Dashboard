package com.andamiro.Dashboard.Security;

import com.andamiro.Dashboard.Entity.User;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.UUID;

@Component
public class JwtTokenProvider {

    private final SecretKey key;
    private final long validityInMs;

    public JwtTokenProvider(
            @Value("${jwt.secret}") String secret,
            @Value("${jwt.expiration-ms:3600000}") long validityInMs) {
        this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.validityInMs = validityInMs;
    }

    public String createToken(UUID userId, User.Role role) {
        Date now = new Date();
        Date expiry = new Date(now.getTime() + validityInMs);

        return Jwts.builder()
                .setSubject(userId.toString())        // 토큰의 "주체" = 유저 id (문자열)
                .claim("role", role.name())           //  커스텀 claim에 역할 박제
                .setIssuedAt(now)                     // 발급 시각
                .setExpiration(expiry)                // 만료 시각
                .signWith(key, SignatureAlgorithm.HS256) // HMAC-SHA256 서명
                .compact();                           // 문자열 토큰 완성
    }

    public boolean validateToken(String token) {
        try {
            parseClaims(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            System.out.println("❌ JWT 검증 실패: " + e.getMessage());
            return false;
        }
    }

    public UUID getUserId(String token) {
        Claims claims = Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody();
        return UUID.fromString(claims.getSubject()); // ✅ 이렇게 돼야 함
    }

    public User.Role getRole(String token) {
        String role = (String) parseClaims(token).get("role");
        return User.Role.valueOf(role);
    }

    public Authentication getAuthentication(String token) {
        // 필요시 UserDetailsService 연동 가능. 여기선 심플하게 ID/ROLE만 담아 인증 생성
        UUID userId = getUserId(token);
        User.Role role = getRole(token);
        return new UsernamePasswordAuthenticationToken(
                userId.toString(),  // principal (간단화)
                null,               // credentials
                java.util.List.of(() -> "ROLE_" + role.name())
        );
    }

    private Claims parseClaims(String token) {
        return Jwts.parserBuilder().setSigningKey(key).build()
                .parseClaimsJws(token).getBody();
    }
}
