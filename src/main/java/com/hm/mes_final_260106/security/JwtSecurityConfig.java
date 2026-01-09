package com.hm.mes_final_260106.security;

import lombok.RequiredArgsConstructor;
import org.springframework.security.config.annotation.SecurityConfigurerAdapter;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.DefaultSecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@RequiredArgsConstructor
public class JwtSecurityConfig extends SecurityConfigurerAdapter<DefaultSecurityFilterChain, HttpSecurity> {
    private final TokenProvider tokenProvider;

    @Override
    public void configure(HttpSecurity http) {
        // JwtFilter를 생성하여 시큐리티 필터 체인에 등록
        JwtFilter customFilter = new JwtFilter(tokenProvider);
        // UsernamePasswordAuthenticationFilter보다 먼저 실행되도록 설정
        http.addFilterBefore(customFilter, UsernamePasswordAuthenticationFilter.class);
    }
}
