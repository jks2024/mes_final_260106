package com.hm.mes_final_260106.dto;
// 자재 입고 요청용 : 외부에서 자재를 입고 하기 위한 DTO

import lombok.Data;

@Data
public class MaterialInboundDto {
    private String code;  // 자재 코드
    private String name;  // 자재 이름
    private int amount;   // 수량
}
