package com.hm.mes_final_260106.entity;
// 작업 지시 테이블
// "무엇을, 몇 개를, 언제부터 생산할 것인가"를 정의 하는 실행 단위
// ERP의 생산 계획을 현장에서 실행 가능한 명령으로 바꾼 결과물

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Getter @Setter @AllArgsConstructor @NoArgsConstructor @Builder
public class WorkOrder {
    @Id
    @GeneratedValue (strategy = GenerationType.IDENTITY)
    private Long id;
    private String productCode; // 생산할 제품
    private int targetQty;      // 목표 수량
    private int currentQty;     // 현재 생산량
    private String status;      // WAITING, IN_PROGRESS, COMPLETED

    private String assignedMachineId;  // 할당 설비 정보
    private LocalDateTime createdAt;  // 생산 시점

    @PrePersist
    public void prePersist(){
        createdAt = LocalDateTime.now();
    }
}
