package com.hm.mes_final_260106.security;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class WebSecurityConfig {
    private final TokenProvider tokenProvider;
    private final JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;
    private final JwtAccessDeniedHandler jwtAccessDeniedHandler;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .csrf(csrf -> csrf.disable()) // CSRF 비활성화
                .cors(Customizer.withDefaults()) // CORS 기본 설정
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)) // 무상태성 유지
                .exceptionHandling(exception -> exception
                        .authenticationEntryPoint(jwtAuthenticationEntryPoint)
                        .accessDeniedHandler(jwtAccessDeniedHandler)
                )
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/auth/**").permitAll() // 로그인, 회원가입 허용
                        // MES 특화 권한 설정
                        .requestMatchers("/api/mes/order/**").hasRole("ADMIN") // 관리자만 지시 가능
                        .requestMatchers("/api/mes/material/**").hasRole("ADMIN") // 관리자만 자재 입고 가능
                        .requestMatchers("/api/mes/machine/**").hasAnyRole("OPERATOR", "ADMIN") // 작업자/관리자 실적 보고 가능
                        .anyRequest().authenticated()
                )
                .apply(new JwtSecurityConfig(tokenProvider));

        return http.build();
    }

    // CORS 허용 정책 설정
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();

        configuration.addAllowedOrigin("http://localhost:3000"); // 리액트 주소 허용
        configuration.addAllowedHeader("*"); // 모든 헤더 허용 (Authorization 등)
        configuration.addAllowedMethod("*"); // 모든 메서드 허용 (GET, POST, OPTIONS 등)
        configuration.setAllowCredentials(true); // 내 자격 증명(쿠키/토큰) 허용

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration); // 모든 경로에 대해 적용
        return source;
    }


}
