package com.hm.mes_final_260106.security;
// 토큰에서 유저 정보를 추출

import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

@Slf4j
public class SecurityUtil {
    private SecurityUtil(){}

    // SecurityContext에서 현재 인증된 사용자의 ID(Long)를 추출하는 메서드
    public static Long getCurrentMemberId() {
        final Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || authentication.getName() == null) {
            log.error("Security Context에 인증 정보가 없습니다.");
            throw new RuntimeException("인증 정보가 없습니다.");
        }

        // TokenProvider에서 Subject에 memberId를 넣어두었으므로 이를 Long으로 변환
        try {
            return Long.parseLong(authentication.getName());
        } catch (NumberFormatException e) {
            log.error("인증 정보의 형식이 올바르지 않습니다: {}", authentication.getName());
            throw new RuntimeException("잘못된 인증 정보입니다.");
        }
    }
}
