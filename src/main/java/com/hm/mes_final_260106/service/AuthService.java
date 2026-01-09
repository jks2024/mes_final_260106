package com.hm.mes_final_260106.service;

import com.hm.mes_final_260106.dto.LoginReqDto;
import com.hm.mes_final_260106.dto.MemberResDto;
import com.hm.mes_final_260106.dto.SignUpReqDto;
import com.hm.mes_final_260106.dto.TokenDto;
import com.hm.mes_final_260106.entity.Member;
import com.hm.mes_final_260106.repository.MemberRepository;
import com.hm.mes_final_260106.security.CustomUserDetailService;
import com.hm.mes_final_260106.security.TokenProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class AuthService {
    private final AuthenticationManagerBuilder managerBuilder;
    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;
    private final TokenProvider tokenProvider;

    // 회원 가입
    public MemberResDto signUp(SignUpReqDto dto) {
        if (memberRepository.existsByEmail(dto.getEmail())) {
            throw new RuntimeException("이미 가입되어 있는 사원 입니다.");
        }
        Member member = dto.toEntity(passwordEncoder);
        memberRepository.save(member);
        return MemberResDto.of(member);
    }

    // 로그인
    @Transactional (readOnly = true)
    public TokenDto login(LoginReqDto dto) {
        UsernamePasswordAuthenticationToken authenticationToken = dto.toAuthenticationToken();
        Authentication authentication = managerBuilder.getObject().authenticate(authenticationToken);
        return tokenProvider.generateTokenDto(authentication);
    }

    // access token 재발급
}
