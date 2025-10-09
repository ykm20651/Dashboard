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

        String requestURI = request.getRequestURI();
        System.out.println("🔍 JWT 필터 진입: " + request.getMethod() + " " + requestURI);

        // ✅ CORS preflight OPTIONS 요청은 즉시 통과
        if (request.getMethod().equalsIgnoreCase("OPTIONS")) {
            response.setHeader("Access-Control-Allow-Origin", "*");
            response.setHeader("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS");
            response.setHeader("Access-Control-Allow-Headers", "Content-Type, Authorization");
            response.setHeader("Access-Control-Allow-Credentials", "false");  // CORS 일관성 유지
            response.setStatus(HttpServletResponse.SC_OK);
            System.out.println("✅ OPTIONS 요청 통과 (CORS Preflight)");
            return;
        }

        // ✅ 화이트리스트 경로 (JWT 없이 통과)
        if ((requestURI.equals("/users") && request.getMethod().equals("POST"))
                || requestURI.equals("/users/login")
                || requestURI.matches("/users/[^/]+/owner-info")
                || requestURI.matches("/users/[^/]+/crew-info")
                || requestURI.startsWith("/swagger-ui")
                || requestURI.startsWith("/v3/api-docs")
                || requestURI.startsWith("/static/")
                || requestURI.startsWith("/css/")
                || requestURI.startsWith("/js/")
                || requestURI.startsWith("/images/")) {
            System.out.println("✅ JWT 필터 화이트리스트 통과: " + requestURI);
            filterChain.doFilter(request, response);
            return;
        }

        // ✅ Authorization 헤더에서 JWT 추출
        String token = resolveToken(request);

        // ✅ JWT 검증 및 SecurityContext 설정
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
        }

        // ✅ 다음 필터로 전달
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
