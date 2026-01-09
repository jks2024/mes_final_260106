package com.hm.mes_final_260106.dto;

import com.hm.mes_final_260106.constant.Authority;
import com.hm.mes_final_260106.entity.Member;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class MemberResDto {
    private String email;
    private String name;
    private Authority authority;

    public static MemberResDto of(Member member) {
        return MemberResDto.builder()
                .email(member.getEmail())
                .name(member.getName())
                .authority(member.getAuthority())
                .build();
    }
}