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
                .cors(cors -> cors.configurationSource(corsConfigurationSource())) // CORS 설정 추가
                .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS)) // ✅ 추가
                .authorizeHttpRequests(auth -> auth
                        // 공개 페이지
                        .requestMatchers("/", "/login", "/signup").permitAll()
                        .requestMatchers("/users/login", "/users", "/swagger-ui/**", "/v3/api-docs/**").permitAll()
                        
                        // 인증 필요 페이지
                        .requestMatchers("/incidents", "/incident-register", "/incident-detail", 
                                       "/report", "/evidence", "/response-guide").authenticated()

                        // 관리자 전용
                        .requestMatchers(HttpMethod.PATCH, "/users/*/approve").hasRole("ADMIN")

                        // 선주 전용
                        .requestMatchers(HttpMethod.POST, "/users/*/owner-info").hasRole("OWNER")
                        .requestMatchers(HttpMethod.GET,    "/incidents/*").hasRole("OWNER")
                        .requestMatchers(HttpMethod.PUT,    "/incidents/*").hasRole("OWNER")
                        .requestMatchers(HttpMethod.DELETE, "/incidents/*").hasRole("OWNER")
                        .requestMatchers("/incidents/*/analyze").hasRole("OWNER")
                        .requestMatchers("/incidents/*/evidence-files").hasRole("OWNER")
                        .requestMatchers("/evidence-files/*").hasRole("OWNER")
                        .requestMatchers("/incidents/*/reports").hasRole("OWNER")
                        .requestMatchers("/incidents/*/response-guide").hasRole("OWNER")

                        // 선원 전용
                        .requestMatchers(HttpMethod.POST, "/users/*/crew-info").hasRole("CREW")

                        // 선주 + 선원 공용
                        .requestMatchers(HttpMethod.PATCH,  "/users/*").hasAnyRole("OWNER","CREW")
                        .requestMatchers(HttpMethod.GET,    "/users/*").hasAnyRole("OWNER","CREW")
                        .requestMatchers(HttpMethod.DELETE, "/users/*").hasAnyRole("OWNER","CREW")

                        .requestMatchers(HttpMethod.GET,  "/incidents").hasAnyRole("OWNER","CREW")
                        .requestMatchers(HttpMethod.POST, "/incidents").hasAnyRole("OWNER","CREW")
                        .requestMatchers(HttpMethod.GET,  "/incidents/*/reports").hasAnyRole("OWNER","CREW")

                        // 증거자료 업로드 (둘 다 가능)
                        .requestMatchers(HttpMethod.POST, "/incidents/*/evidence-files").hasAnyRole("OWNER","CREW")

                        // 나머지는 인증만
                        .anyRequest().authenticated()
                )
                .exceptionHandling(ex -> ex
                        // 인증 안 된 상태일 때는 무조건 401 Unauthorized
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

    //CORS - Cross-Origin Resource Sharing - 브라우저는 보안 상 원래 다른 도메인 간 요청을 막음.
    //프론트랑 백api 연동하기 위해서는 백엔드에서 프론트 요청을 허용하겠다고 명시하는 것이 아래 코드임.
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        
        // 허용할 Origin 설정 (EC2 서버 주소와 로컬 개발 환경)
        //configuration.addAllowedOriginPattern("*"); // 모든 도메인 허용 (개발용)
        
        // 운영환경에서는 구체적인 도메인 지정: 
        //프론트 페이지가 EC2 동일 서버의 80포트에서 서비스 중이라면
        configuration.addAllowedOrigin("http://15.164.99.177:80");
        configuration.addAllowedOrigin("http://localhost:3000"); //로컬에서도 프론트에서 백으로 api 요청 가능하도록 함.
        
        // 허용할 HTTP 메서드
        configuration.addAllowedMethod("*"); // 모든 메서드 허용
        
        // 허용할 헤더
        configuration.addAllowedHeader("*"); // 모든 헤더 허용
        
        // 인증 정보 포함 허용 (JWT 토큰 전송을 위해)
        configuration.setAllowCredentials(true);
        
        // Preflight 요청 캐시 시간 (초)
        configuration.setMaxAge(3600L);
        
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        
        return source;
    }
    
    
    
    /* -- 로그인 폼 비활성화--
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())        // CSRF 비활성화
                .authorizeHttpRequests(auth -> auth
                        .anyRequest().permitAll()        // 모든 요청 허용
                )
                .formLogin(form -> form.disable())   // 기본 로그인 폼 비활성화
                .httpBasic(httpBasic -> httpBasic.disable()); // HTTP Basic 인증도 비활성화

        return http.build();
    }
    */
    

    

}
