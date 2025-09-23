package com.andamiro.Dashboard.Security;

import com.andamiro.Dashboard.Entity.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Date;
import java.util.UUID;

@Component
//JWT 발급 및 검증 로직을 담당하는 유틸 클래스 (Spring Bean).
public class JwtTokenProvider {

    private final Key secretKey; //서명용 비밀 키
    private final long validityInMilliseconds; //토큰 유효시간

    // 설정값을 주입받아 Key를 생성
    public JwtTokenProvider(@Value("${jwt.secret}") String secret, @Value("${jwt.validity-ms}") long validityInMilliseconds) {
        // HS256은 최소 32바이트 이상 키 필요
        this.secretKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.validityInMilliseconds = validityInMilliseconds;
    }

    /** 토큰 생성 */
    public String createToken(UUID userId, User.Role role) {
        Date now = new Date();
        Date exp = new Date(now.getTime() + validityInMilliseconds);

        return Jwts.builder()
                .setSubject(userId.toString())
                .claim("role", role.name())
                .setIssuedAt(now)
                .setExpiration(exp)
                .signWith(secretKey, SignatureAlgorithm.HS256) // 최신 JJWT 문법
                .compact();
    }

    /** 토큰 검증 */
    public boolean validateToken(String token) {
        try {
            Jwts.parserBuilder()
                    .setSigningKey(secretKey)
                    .build()
                    .parseClaimsJws(token);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    /** 토큰에서 사용자 ID 추출 */
    public UUID getUserId(String token) {
        Claims claims = Jwts.parserBuilder()
                .setSigningKey(secretKey)
                .build()
                .parseClaimsJws(token)
                .getBody();
        return UUID.fromString(claims.getSubject());
    }

    public User.Role getRole(String token) {
        Claims claims = Jwts.parserBuilder()
                .setSigningKey(secretKey)
                .build()
                .parseClaimsJws(token)
                .getBody();
        return User.Role.valueOf(claims.get("role", String.class));
    }
}
