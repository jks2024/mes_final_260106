package com.hm.mes_final_260106.controller;

import com.hm.mes_final_260106.dto.MaterialInboundDto;
import com.hm.mes_final_260106.dto.ProductionReportDto;
import com.hm.mes_final_260106.dto.WorkOrderReqDto;
import com.hm.mes_final_260106.dto.WorkOrderResDto;
import com.hm.mes_final_260106.entity.Material;
import com.hm.mes_final_260106.entity.WorkOrder;
import com.hm.mes_final_260106.service.ProductionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
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
    @GetMapping("/material/stock")
    public ResponseEntity<List<Material>> getMaterialStock() {
        return ResponseEntity.ok(productionService.getMaterialStock());
    }

    // 작업 지시 생성 API : 계획 입력(React) -> DB에 레코드 추가 -> 반환(DTO) -> 대시보드에 현재상태가 WAIT로 표시 되면서 작없이 등록 됨
    @PostMapping("/order")
    public ResponseEntity<WorkOrderResDto> createOrder(@RequestBody WorkOrderReqDto dto) {
        WorkOrder order = productionService.createWorkOrder(dto.getProductCode(), dto.getTargetQty());
        return ResponseEntity.ok(WorkOrderResDto.fromEntity(order));
    }

    // Dashboard : 작업 지시 목록 조회 API
    @GetMapping("/orders")
    public ResponseEntity<List<WorkOrderResDto>> getAllOrders() {
        return ResponseEntity.ok(productionService.getAllWorkOrders().stream().map(WorkOrderResDto::fromEntity).toList());
    }

    // Machine : 설비 작업 할당, 설비에서 폴링 방식으로 주기적인 체크
    @GetMapping("machine/poll")
    public ResponseEntity<WorkOrderResDto> pollWork(@RequestParam String machineId) {
        WorkOrder work = productionService.assignWorkToMachine(machineId);
        return (work != null) ? ResponseEntity.ok(WorkOrderResDto.fromEntity(work)) : ResponseEntity.noContent().build();
    }

    // Machine : 생산 결과 보고
    @PostMapping("/machine/report")
    public ResponseEntity<String> reportProduction(@RequestBody ProductionReportDto dto) {
        productionService.reportProduction(dto.getOrderId(), dto.getMachineId(), dto.getResult(), dto.getDefectCode());
        return ResponseEntity.ok("ACK");
    }
}
