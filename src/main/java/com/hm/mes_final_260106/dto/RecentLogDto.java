package com.hm.mes_final_260106.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
public class RecentLogDto {
    private Long id;
    private String serialNo;
    private String operatorName; // Member 엔티티에서 name 추출
    private String result;
    private String machineId;
    private LocalDateTime producedAt;
}
