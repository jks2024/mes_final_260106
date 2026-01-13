package com.hm.mes_final_260106.service;

import com.hm.mes_final_260106.dto.LoginReqDto;
import com.hm.mes_final_260106.dto.MemberResDto;
import com.hm.mes_final_260106.dto.SignUpReqDto;
import com.hm.mes_final_260106.dto.TokenDto;
import com.hm.mes_final_260106.entity.Member;
import com.hm.mes_final_260106.entity.RefreshToken;
import com.hm.mes_final_260106.repository.MemberRepository;
import com.hm.mes_final_260106.repository.RefreshTokenRepository;
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
    private final RefreshTokenRepository refreshTokenRepository;

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
    @Transactional
    public TokenDto login(LoginReqDto dto) {
        UsernamePasswordAuthenticationToken authenticationToken = dto.toAuthenticationToken();
        Authentication authentication = managerBuilder.getObject().authenticate(authenticationToken);

        // 토큰 생성
        TokenDto tokenDto = tokenProvider.generateTokenDto(authentication);

        // 1:1 연관 관계를 통한 Refresh Token 관리
        Member member = memberRepository.findById(Long.parseLong(authentication.getName()))
                .orElseThrow(() -> new RuntimeException("사용자 정보를 찾을 수 없습니다."));

        // 기존 토큰이 있으면 업데이트, 없으면 신규 생성
        RefreshToken refreshToken = refreshTokenRepository.findByMember(member)
                .map(token -> {
                    token.updateValue(tokenDto.getRefreshToken());
                    return token;
                })
                .orElse(new RefreshToken(member, tokenDto.getRefreshToken()));

        refreshTokenRepository.save(refreshToken);
        return tokenDto;
    }

    // access token 재발급
    public TokenDto refresh(TokenDto tokenRequestDto) {
        // 1. 전달받은 Refresh Token의 형태(서명 등) 검증
        if (!tokenProvider.validateToken(tokenRequestDto.getRefreshToken())) {
            throw new RuntimeException("전달된 Refresh Token이 유효하지 않습니다.");
        }

        // 2. Access Token에서 사용자 식별 정보(ID) 추출
        Authentication authentication = tokenProvider.getAuthentication(tokenRequestDto.getAccessToken());
        Long memberId = Long.parseLong(authentication.getName());

        // 3. DB에 저장된 1:1 매핑된 토큰 조회
        RefreshToken refreshToken = refreshTokenRepository.findByMemberId(memberId)
                .orElseThrow(() -> new RuntimeException("로그아웃 상태이거나 토큰이 존재하지 않습니다."));

        // 4. DB의 토큰과 클라이언트가 보낸 토큰이 일치하는지 보안 검사
        if (!refreshToken.getToken().equals(tokenRequestDto.getRefreshToken())) {
            throw new RuntimeException("토큰 정보가 일치하지 않습니다. 다시 로그인해주세요.");
        }

        // 5. 새로운 토큰 세트(Access + Refresh) 생성
        TokenDto newTokenDto = tokenProvider.generateTokenDto(authentication);

        // 6. DB 값 업데이트 (Token Rotation)
        refreshToken.updateValue(newTokenDto.getRefreshToken());

        return newTokenDto;
    }
}
