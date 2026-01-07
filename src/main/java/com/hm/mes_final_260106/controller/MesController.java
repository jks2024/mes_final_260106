package com.hm.mes_final_260106.controller;

import com.hm.mes_final_260106.dto.MaterialInboundDto;
import com.hm.mes_final_260106.entity.Material;
import com.hm.mes_final_260106.service.ProductionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
// 웹 대시 보드 및 설비를 연결


@RestController
@RequestMapping("/api/mes")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:3000")
@Slf4j
public class MesController {
    private final ProductionService productionService;

    // Dashboard : 자재 입고 API, 자재가 입고 됨을 알려줌, 생산 이전 상태
    @PostMapping("/material/inbound")
    public ResponseEntity<Material> inboundMaterial(@RequestBody MaterialInboundDto dto) {
        log.info("자재 입고 : {}", dto);
        return ResponseEntity.ok(productionService.inboundMaterial(dto.getCode(), dto.getName(), dto.getAmount()));
    }
    // Dashboard : 자재 재고 조회 API

    // 작업 지시 생성 API

    // 작업 지시 목록 조회 API

    // Machine : 설비 작업 할당

    // Machine : 생산 결과 보고

}
