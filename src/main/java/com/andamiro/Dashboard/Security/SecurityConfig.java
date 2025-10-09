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

@Configuration
public class SecurityConfig {

    private final JwtTokenProvider jwtTokenProvider;

    public SecurityConfig(JwtTokenProvider jwtTokenProvider) {
        this.jwtTokenProvider = jwtTokenProvider;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .headers(headers -> headers.frameOptions(frameOptions -> frameOptions.disable()))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                        // [1] 정적 리소스는 모두 허용
                        .requestMatchers(
                                "/", 
                                "/index.html", 
                                "/login.html", 
                                "/signup.html",
                                "/about.html",
                                "/product.html",
                                "/bm.html",
                                "/contact.html",
                                "/css/**", 
                                "/js/**", 
                                "/images/**", 
                                "/static/**"
                        ).permitAll()

                        // [2] 로그인 / 회원가입 / 추가 정보 입력 / Swagger 공개 API
                        .requestMatchers(
                                "/users/login", 
                                "/users", 
                                "/users/*/owner-info",  // 선주 추가 정보 입력 허용
                                "/users/*/crew-info",   // 선원 추가 정보 입력 허용
                                "/swagger-ui/**", 
                                "/v3/api-docs/**"
                        ).permitAll()

                        


                        // [3] 관리자 전용
                        .requestMatchers(HttpMethod.PATCH, "/users/*/approve").hasRole("ADMIN")

                        // [4] 선주 전용 (추가 정보 입력은 permitAll로 이동됨)
                        // .requestMatchers(HttpMethod.POST, "/users/*/owner-info").hasRole("OWNER")
                        .requestMatchers(HttpMethod.GET,    "/incidents/*").hasRole("OWNER")
                        .requestMatchers(HttpMethod.PUT,    "/incidents/*").hasRole("OWNER")
                        .requestMatchers(HttpMethod.DELETE, "/incidents/*").hasRole("OWNER")
                        .requestMatchers("/incidents/*/analyze").hasRole("OWNER")
                        .requestMatchers("/incidents/*/evidence-files").hasRole("OWNER")
                        .requestMatchers("/evidence-files/*").hasRole("OWNER")
                        .requestMatchers("/incidents/*/reports").hasRole("OWNER")
                        .requestMatchers("/incidents/*/response-guide").hasRole("OWNER")

                        // [5] 선원 전용 (추가 정보 입력은 permitAll로 이동됨)
                        // .requestMatchers(HttpMethod.POST, "/users/*/crew-info").hasRole("CREW")

                        // [6] 선주 + 선원 공용
                        .requestMatchers(HttpMethod.PATCH,  "/users/*").hasAnyRole("OWNER","CREW")
                        .requestMatchers(HttpMethod.GET,    "/users/*").hasAnyRole("OWNER","CREW")
                        .requestMatchers(HttpMethod.DELETE, "/users/*").hasAnyRole("OWNER","CREW")
                        .requestMatchers(HttpMethod.GET,  "/incidents").hasAnyRole("OWNER","CREW")
                        .requestMatchers(HttpMethod.POST, "/incidents").hasAnyRole("OWNER","CREW")
                        .requestMatchers(HttpMethod.GET,  "/incidents/*/reports").hasAnyRole("OWNER","CREW")
                        .requestMatchers(HttpMethod.POST, "/incidents/*/evidence-files").hasAnyRole("OWNER","CREW")

                        // [7] 그 외 요청은 인증만 필요
                        .anyRequest().authenticated()
                )
                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint((req, res, e) ->
                                res.sendError(HttpServletResponse.SC_UNAUTHORIZED, "인증이 필요합니다."))
                )
                .addFilterBefore(new JwtAuthenticationFilter(jwtTokenProvider),
                        UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    // CORS 설정
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {  
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.addAllowedOriginPattern("*");  // 모든 도메인 허용
        configuration.addAllowedMethod("*");
        configuration.addAllowedHeader("*");
        configuration.setAllowCredentials(true);
        configuration.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);

        return source;
    }
}
