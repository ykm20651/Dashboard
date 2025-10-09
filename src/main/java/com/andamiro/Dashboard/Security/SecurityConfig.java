package com.andamiro.Dashboard.Security;

import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
public class SecurityConfig {

    private final JwtTokenProvider jwtTokenProvider;

    public SecurityConfig(JwtTokenProvider jwtTokenProvider) {
        this.jwtTokenProvider = jwtTokenProvider;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                // [1] CORS + CSRF 비활성화
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .csrf(csrf -> csrf.disable())

                // [2] 세션 비활성화 (JWT 기반)
                .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

                // [3] 프레임옵션 비활성화 (H2 콘솔 등)
                .headers(headers -> headers.frameOptions(frame -> frame.disable()))

                // [4] 요청별 권한 설정
                .authorizeHttpRequests(auth -> auth
                        //  프리플라이트 요청 전부 허용
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()

                        //  정적 리소스 허용
                        .requestMatchers(
                                "/", "/index.html",
                                "/login.html", "/signup.html", "/about.html",
                                "/product.html", "/bm.html", "/contact.html",
                                "/css/**", "/js/**", "/images/**", "/static/**"
                        ).permitAll()

                        //  인증 없이 접근 가능한 공개 API (회원가입 포함)
                        .requestMatchers(
                                "/users/login",
                                "/users/signup",     //  추가됨: 회원가입 허용
                                "/users",
                                "/users/*/owner-info",
                                "/users/*/crew-info",
                                "/swagger-ui/**",
                                "/v3/api-docs/**"
                        ).permitAll()

                        //  관리자 전용
                        .requestMatchers(HttpMethod.PATCH, "/users/*/approve").hasRole("ADMIN")

                        //  선주 전용
                        .requestMatchers(HttpMethod.GET, "/incidents/*").hasRole("OWNER")
                        .requestMatchers(HttpMethod.PUT, "/incidents/*").hasRole("OWNER")
                        .requestMatchers(HttpMethod.DELETE, "/incidents/*").hasRole("OWNER")
                        .requestMatchers("/incidents/*/analyze").hasRole("OWNER")
                        .requestMatchers("/incidents/*/evidence-files").hasRole("OWNER")
                        .requestMatchers("/evidence-files/*").hasRole("OWNER")
                        .requestMatchers("/incidents/*/reports").hasRole("OWNER")
                        .requestMatchers("/incidents/*/response-guide").hasRole("OWNER")

                        //  선원 전용
                        .requestMatchers(HttpMethod.PATCH, "/users/*").hasAnyRole("OWNER", "CREW")
                        .requestMatchers(HttpMethod.GET, "/users/*").hasAnyRole("OWNER", "CREW")
                        .requestMatchers(HttpMethod.DELETE, "/users/*").hasAnyRole("OWNER", "CREW")
                        .requestMatchers(HttpMethod.GET, "/incidents").hasAnyRole("OWNER", "CREW")
                        .requestMatchers(HttpMethod.POST, "/incidents").hasAnyRole("OWNER", "CREW")
                        .requestMatchers(HttpMethod.GET, "/incidents/*/reports").hasAnyRole("OWNER", "CREW")
                        .requestMatchers(HttpMethod.POST, "/incidents/*/evidence-files").hasAnyRole("OWNER", "CREW")

                        //  그 외는 인증 필요
                        .anyRequest().authenticated()
                )

                // [5] 인증 실패 시 처리
                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint((req, res, e) ->
                                res.sendError(HttpServletResponse.SC_UNAUTHORIZED, "인증이 필요합니다."))
                );

        return http.build();
    }

    // [6] 패스워드 인코더
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    // [7] CORS 전역 설정
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOriginPatterns(List.of("*"));
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(List.of("Authorization", "Content-Type"));
        configuration.setAllowCredentials(true);
        configuration.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}
