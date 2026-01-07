package com.hm.mes_final_260106.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                // 1. CSRF 보호 비활성화 (REST API에서는 보통 끕니다)
                .csrf(csrf -> csrf.disable())

                // 2. CORS 설정 (이미 Controller에 @CrossOrigin이 있지만, 여기서도 허용 설정)
                .cors(cors -> cors.configure(http))

                // 3. 요청 권한 설정
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/api/mes/**").permitAll() // 해당 경로는 로그인 없이 허용
                        .anyRequest().authenticated()               // 나머지는 로그인 필요
                );

        return http.build();
    }
}