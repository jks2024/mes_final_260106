package com.hm.mes_final_260106.dto;
// 생산 실적 보고 : 현장에서 생산한 1건의 생산 결과를 시스템에 보고하기 위한 입력 DTO
// 설비(설비 <-> 수집기) <-> 서버

import lombok.Data;

@Data
public class ProductionReportDto {
    private Long orderId;  // 작업 지시 ID
    private String machineId;  // 설비 ID
    private String result;  // OK or NG
    private String defectCode;  // 불량 코드
}
