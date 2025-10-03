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
                .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS)) // ✅ 추가
                .authorizeHttpRequests(auth -> auth
                        // 공개
                        .requestMatchers("/users/login", "/users", "/swagger-ui/**", "/v3/api-docs/**").permitAll()

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
