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

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        String method = request.getMethod();
        String requestURI = request.getRequestURI();

        System.out.println("🔍 JWT 필터 진입: " + method + " " + requestURI);

        // ✅ 1. OPTIONS (CORS Preflight) 요청은 바로 통과
        if (method.equalsIgnoreCase("OPTIONS")) {
            response.setHeader("Access-Control-Allow-Origin", "*");
            response.setHeader("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS");
            response.setHeader("Access-Control-Allow-Headers", "Content-Type, Authorization");
            response.setHeader("Access-Control-Allow-Credentials", "false");
            response.setStatus(HttpServletResponse.SC_OK);
            System.out.println("✅ OPTIONS 요청 통과 (CORS Preflight)");
            return;
        }

        // ✅ 2. 화이트리스트 경로 — JWT 없이 통과
        if (isWhitelisted(requestURI, method)) {
            System.out.println("✅ JWT 필터 화이트리스트 통과: " + requestURI);
            filterChain.doFilter(request, response);
            return;
        }

        // ✅ 3. Authorization 헤더에서 JWT 토큰 추출
        String token = resolveToken(request);

        // ✅ 4. JWT 검증 및 SecurityContext 등록
        if (token != null && jwtTokenProvider.validateToken(token)) {
            UUID userId = jwtTokenProvider.getUserId(token);
            User.Role role = jwtTokenProvider.getRole(token);

            UsernamePasswordAuthenticationToken authentication =
                    new UsernamePasswordAuthenticationToken(
                            userId,
                            null,
                            List.of(new SimpleGrantedAuthority("ROLE_" + role.name()))
                    );

            SecurityContextHolder.getContext().setAuthentication(authentication);
            System.out.println("✅ JWT 인증 성공: userId=" + userId + ", role=" + role);
        } else if (token != null) {
            System.out.println("❌ 잘못된 JWT 토큰 또는 만료됨");
        }

        // ✅ 5. 다음 필터로 전달
        filterChain.doFilter(request, response);
    }

    /**
     * JWT 화이트리스트 정의
     * 회원가입, 로그인, Swagger, 정적 리소스 등은 토큰 없이 접근 가능
     */
    private boolean isWhitelisted(String uri, String method) {
        // 회원가입 + 로그인
        if (uri.equals("/users") && method.equalsIgnoreCase("POST")) return true;
        if (uri.equals("/users/login") && method.equalsIgnoreCase("POST")) return true;

        // 공개 API
        if (uri.startsWith("/swagger-ui")) return true;
        if (uri.startsWith("/v3/api-docs")) return true;

        // 정적 리소스 (HTML, JS, CSS, 이미지 등)
        if (uri.startsWith("/static/")) return true;
        if (uri.startsWith("/css/")) return true;
        if (uri.startsWith("/js/")) return true;
        if (uri.startsWith("/images/")) return true;
        if (uri.equals("/") || uri.endsWith(".html")) return true;

        return false;
    }

    /**
     * Authorization 헤더에서 Bearer 토큰 추출
     */
    private String resolveToken(HttpServletRequest request) {
        String bearer = request.getHeader("Authorization");
        if (bearer != null && bearer.startsWith("Bearer ")) {
            return bearer.substring(7);
        }
        return null;
    }
}
