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
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
public class SecurityConfig {

    private final JwtTokenProvider jwtTokenProvider;
    public SecurityConfig(JwtTokenProvider jwtTokenProvider) { this.jwtTokenProvider = jwtTokenProvider; }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .cors(c -> c.configurationSource(corsConfigurationSource()))
            .csrf(c -> c.disable())
            .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

            .authorizeHttpRequests(auth -> auth
                // 0) 프리플라이트
                .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()

                // 1) 회원가입/로그인 허용
                .requestMatchers(HttpMethod.POST, "/users").permitAll()
                .requestMatchers(HttpMethod.POST, "/users/login").permitAll()

                // 2) 하위 공개 엔드포인트(추가 정보 등)도 전부 허용
                .requestMatchers("/users/**").permitAll()

                // 3) 정적 리소스 & 문서
                .requestMatchers("/", "/**/*.html",
                        "/css/**", "/js/**", "/images/**", "/static/**",
                        "/swagger-ui/**", "/v3/api-docs/**").permitAll()

                // 4) 나머지 제한
                .requestMatchers(HttpMethod.PATCH, "/users/*/approve").hasRole("ADMIN")

                .requestMatchers(HttpMethod.GET, "/incidents/*").hasRole("OWNER")
                .requestMatchers(HttpMethod.PUT, "/incidents/*").hasRole("OWNER")
                .requestMatchers(HttpMethod.DELETE, "/incidents/*").hasRole("OWNER")
                .requestMatchers("/incidents/*/analyze").hasRole("OWNER")
                .requestMatchers("/incidents/*/evidence-files").hasRole("OWNER")
                .requestMatchers("/evidence-files/*").hasRole("OWNER")
                .requestMatchers("/incidents/*/reports").hasRole("OWNER")
                .requestMatchers("/incidents/*/response-guide").hasRole("OWNER")

                .requestMatchers(HttpMethod.PATCH, "/users/*").hasAnyRole("OWNER","CREW")
                .requestMatchers(HttpMethod.GET, "/users/*").hasAnyRole("OWNER","CREW")
                .requestMatchers(HttpMethod.DELETE, "/users/*").hasAnyRole("OWNER","CREW")
                .requestMatchers(HttpMethod.GET, "/incidents").hasAnyRole("OWNER","CREW")
                .requestMatchers(HttpMethod.POST, "/incidents").hasAnyRole("OWNER","CREW")
                .requestMatchers(HttpMethod.GET, "/incidents/*/reports").hasAnyRole("OWNER","CREW")
                .requestMatchers(HttpMethod.POST, "/incidents/*/evidence-files").hasAnyRole("OWNER","CREW")

                .anyRequest().authenticated()
            )

            .exceptionHandling(ex -> ex
                .authenticationEntryPoint((req, res, e) -> {
                    // 왜 401이 나는지 바로 확인할 수 있게 헤더/메시지 추가
                    res.setHeader("X-Auth-Fail", "security");
                    res.sendError(HttpServletResponse.SC_UNAUTHORIZED, "인증이 필요합니다.");
                })
            )

            // 필터 순서: UsernamePasswordAuthenticationFilter 전에 배치
            .addFilterBefore(new JwtAuthenticationFilter(jwtTokenProvider),
                    UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() { return new BCryptPasswordEncoder(); }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration cfg = new CorsConfiguration();
        cfg.setAllowedOriginPatterns(List.of("*"));
        cfg.setAllowedMethods(List.of("GET","POST","PUT","DELETE","OPTIONS"));
        // 프런트에서 cache-control/pragma 헤더도 보내므로 허용 목록에 추가
        cfg.setAllowedHeaders(List.of("Authorization","Content-Type","Cache-Control","Pragma"));
        cfg.setAllowCredentials(false);
        cfg.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", cfg);
        return source;
    }
}
