package com.hm.mes_final_260106.controller;

import com.hm.mes_final_260106.dto.LoginReqDto;
import com.hm.mes_final_260106.dto.MemberResDto;
import com.hm.mes_final_260106.dto.SignUpReqDto;
import com.hm.mes_final_260106.dto.TokenDto;
import com.hm.mes_final_260106.service.AuthService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@Slf4j
public class AuthController {
    private final AuthService authService;

    // 회원 가입
    @PostMapping("/signup")
    public ResponseEntity<MemberResDto> signup(@RequestBody SignUpReqDto requestDto) {
        log.info("signup requestDto {}", requestDto);
        return ResponseEntity.ok(authService.signUp(requestDto));
    }

    // 로그인
    @PostMapping("/login")
    public ResponseEntity<TokenDto> login(@RequestBody LoginReqDto requestDto) {
        log.info("login requestDto {}", requestDto);
        return ResponseEntity.ok(authService.login(requestDto));
    }

    // 토큰 재발급 : 프론트엔드와 C# 수집기에서 토큰이 만료되었을 때 호출합니다.
    @PostMapping("/refresh")
    public ResponseEntity<TokenDto> refresh(@RequestBody TokenDto tokenRequestDto) {
        log.info("refresh tokenRequestDto {}", tokenRequestDto);
        return ResponseEntity.ok(authService.refresh(tokenRequestDto));
    }

}
