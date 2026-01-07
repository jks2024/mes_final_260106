package com.hm.mes_final_260106.dto;
// 작업 지시 요청 : 어떤 제품을 몇 개 만들건지 전달
// 리액트 대시 보시보드에서 백엔드로 요청
import lombok.Data;

@Data
public class WorkOrderReqDto {
    private String productCode;  // 제품 코드
    private int targetQty;       // 목표 수량
}
