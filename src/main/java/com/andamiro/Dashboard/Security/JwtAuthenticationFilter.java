package com.andamiro.Dashboard.Security;

import com.andamiro.Dashboard.Entity.User;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.lang.NonNull;
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
    protected void doFilterInternal(@NonNull HttpServletRequest request,
                                    @NonNull HttpServletResponse response,
                                    @NonNull FilterChain filterChain)
            throws ServletException, IOException {

        String method = request.getMethod();
        String requestURI = request.getRequestURI();

        System.out.println("[JWT 필터] 요청 감지: " + method + " " + requestURI);
        if (requestURI.equals("/favicon.ico")) {
            filterChain.doFilter(request, response);
            return;
        }

        //  1. Preflight OPTIONS 요청은 즉시 통과
        if (method.equalsIgnoreCase("OPTIONS")) {
            response.setHeader("Access-Control-Allow-Origin", "*");
            response.setHeader("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS");
            response.setHeader("Access-Control-Allow-Headers", "Content-Type, Authorization");
            response.setHeader("Access-Control-Allow-Credentials", "false");
            response.setStatus(HttpServletResponse.SC_OK);
            System.out.println("[JWT 필터] OPTIONS 요청 통과 (CORS)");
            return;
        }

        //  2. 화이트리스트 경로는 JWT 검사 생략
        if (isWhitelisted(requestURI, method)) {
            System.out.println("[JWT 필터] 화이트리스트 경로 통과 → " + requestURI);
            filterChain.doFilter(request, response);
            return;
        }

        //  3. Authorization 헤더 추출
        String token = resolveToken(request);
        if (token == null) {
            System.out.println("[JWT 필터] 토큰 없음 → 인증 불가");
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json;charset=UTF-8");
            response.getWriter().write("{\"error\": \"인증이 필요합니다.\", \"message\": \"토큰이 없습니다.\"}");
            return;
        }

        //  4. JWT 검증
        try {
            System.out.println("[JWT 필터] 토큰 검증 시작: " + token.substring(0, Math.min(20, token.length())) + "...");
            
            if (jwtTokenProvider.validateToken(token)) {
                UUID userId = jwtTokenProvider.getUserId(token);
                User.Role role = jwtTokenProvider.getRole(token);

                CustomPrincipal principal = new CustomPrincipal(userId, role.name());

                UsernamePasswordAuthenticationToken authentication =
                        new UsernamePasswordAuthenticationToken(
                                principal,
                                null,
                                List.of(new SimpleGrantedAuthority("ROLE_" + role.name()))
                        );

                SecurityContextHolder.getContext().setAuthentication(authentication);
                System.out.println("[JWT 필터] 인증 성공: userId=" + userId + ", role=" + role);
            } else {
                System.out.println("[JWT 필터] 토큰 검증 실패 - 유효하지 않은 토큰");
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                response.setContentType("application/json;charset=UTF-8");
                response.getWriter().write("{\"error\": \"인증이 필요합니다.\", \"message\": \"유효하지 않은 토큰입니다.\"}");
                return;
            }
        } catch (Exception e) {
            System.out.println("[JWT 필터] 토큰 처리 중 예외 발생: " + e.getMessage());
            e.printStackTrace();
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json;charset=UTF-8");
            response.getWriter().write("{\"error\": \"인증이 필요합니다.\", \"message\": \"토큰 처리 중 오류가 발생했습니다.\"}");
            return;
        }

        // 5. 다음 필터로 이동
        filterChain.doFilter(request, response);
    }

    /**
     * 화이트리스트 경로 설정
     * 회원가입(/users [POST]), 로그인(/users/login [POST]), 추가 정보 등록, Swagger, 정적 리소스
     */
    private boolean isWhitelisted(String uri, String method) {

        // 회원가입
        if (uri.equals("/users") && method.equalsIgnoreCase("POST")) return true;
        // 로그인
        if (uri.equals("/users/login") && method.equalsIgnoreCase("POST")) return true;
        
        // 추가 정보 등록 (회원가입 과정의 일부)
        if (uri.matches("/users/[^/]+/owner-info") && method.equalsIgnoreCase("POST")) return true;
        if (uri.matches("/users/[^/]+/crew-info") && method.equalsIgnoreCase("POST")) return true;

        // 보고서 다운로드 (AI PDF 파일)
        if (uri.startsWith("/files/")) return true;

        // Swagger
        if (uri.startsWith("/swagger-ui")) return true;
        if (uri.startsWith("/v3/api-docs")) return true;

        if (uri.equals("/favicon.ico")) return true;


        // 정적 리소스
        if (uri.startsWith("/static/") || uri.startsWith("/css/") ||
                uri.startsWith("/js/") || uri.startsWith("/images/")) return true;
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
