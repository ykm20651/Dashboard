package com.andamiro.Dashboard.Security;

import com.andamiro.Dashboard.Entity.User;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtTokenProvider jwtTokenProvider;

    public JwtAuthenticationFilter(JwtTokenProvider jwtTokenProvider) {
        this.jwtTokenProvider = jwtTokenProvider;
    }

    @Override //스프링 시큐리티의 진입점 - doFilterInternal
    //Servlet 컨테이너가 요청을 받으면 여러 보안 필터를 거치는데, 그 중 하나가 이 메서드.
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        // 1. Authorization 헤더에서 토큰 추출 - 별도 메서드(resolveToken)로 분리해서 헤더 파싱.
        String token = resolveToken(request);

        // 2. 토큰 검증
        if (token != null && jwtTokenProvider.validateToken(token)) {
            UUID userId = jwtTokenProvider.getUserId(token);
            User.Role role = jwtTokenProvider.getRole(token); // JwtTokenProvider에 getRole 추가 필요

            // 3. 인증 객체 생성
            UsernamePasswordAuthenticationToken authentication =
                    new UsernamePasswordAuthenticationToken(
                            userId, //Principal
                            null, // Credentials -우린 토큰 기반이라 비밀번호 안씀
                            //name()은 enum 상수 그대로의 문자열을 리턴해.
                            List.of(new SimpleGrantedAuthority("ROLE_" + role.name())) //authorities
                    );

            // 4. SecurityContext에 저장 → 이후 컨트롤러에서 사용 가능
            SecurityContextHolder.getContext().setAuthentication(authentication);
        }

        // 다음 필터로 진행
        filterChain.doFilter(request, response);
    }

    private String resolveToken(HttpServletRequest request) {
        String bearer = request.getHeader("Authorization");
        if (bearer != null && bearer.startsWith("Bearer ")) {
            return bearer.substring(7);
        }
        return null;
    }
}
