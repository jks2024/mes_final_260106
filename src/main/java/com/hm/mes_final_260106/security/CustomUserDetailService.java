package com.hm.mes_final_260106.security;

import com.hm.mes_final_260106.entity.Member;
import com.hm.mes_final_260106.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import java.util.Collections;

// DB에 저장된 정보와 사용자가 입력한 정보를 비교
// 우리가 만든 Member 엔티티는 Spring Security가 직점 이해하지 못하므로 UserDetails라는 표준 인터페이스 객체로 변환

@Service
@RequiredArgsConstructor
public class CustomUserDetailService implements UserDetailsService {
    private final MemberRepository memberRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        // 1. 이메일을 기준으로 사용자를 찾음
        return memberRepository.findByEmail(username)
                .map(this::createUserDetails)
                .orElseThrow(() -> new UsernameNotFoundException(username + " 을 DB에서 찾을 수 없습니다"));
    }

    private UserDetails createUserDetails(Member member) {
        // 2. DB의 권한 정보를 Spring Security가 이해하는 형태로 변환 (예: ROLE_ADMIN)
        GrantedAuthority grantedAuthority = new SimpleGrantedAuthority(member.getAuthority().toString());

        // 3. UserDetails 객체를 생성하여 반환
        //    첫 번째 인자인 Username 자리에 사번/이메일 대신 PK(id)를 넣으면 SecurityUtil에서 쓰기 편함
        return new User(
                String.valueOf(member.getId()),
                member.getPassword(), // 엔티티 필드명이 password라면 getPassword()로 수정
                Collections.singleton(grantedAuthority)
        );
    }
}
