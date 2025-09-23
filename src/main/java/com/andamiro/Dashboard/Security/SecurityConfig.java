package com.andamiro.Dashboard.Security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
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
                .csrf(csrf -> csrf.disable()) //CSRF(Cross-Site Request Forgery)는 보통 세션-쿠키 기반 인증에서만 필요한 보안 기능.
                .authorizeHttpRequests(auth -> auth //“어떤 요청을 허용/차단할지 규칙 세우기” 시작.
                        .requestMatchers("/login", "/swagger-ui/**", "/v3/api-docs/**").permitAll() // /login, /swagger-ui/**, /v3/api-docs/** 경로는 누구나 접근 가능 (로그인 안 해도 됨).
                        .requestMatchers("/owner/**").hasRole("OWNER")
                        .requestMatchers("/crew/**").hasRole("CREW")
                        .anyRequest().authenticated() //위에 명시 안 된 나머지 API는 로그인만 되어 있으면 접근 가능.
                )
                // UsernamePasswordAuthenticationFilter 앞에 우리가 만든 JWT 필터 삽입
                .addFilterBefore(new JwtAuthenticationFilter(jwtTokenProvider),
                        UsernamePasswordAuthenticationFilter.class);

        return http.build(); //필터 체인 완성해서 Spring Security한테 넘김.
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
