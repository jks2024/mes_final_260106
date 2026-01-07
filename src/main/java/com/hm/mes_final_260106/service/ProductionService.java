package com.hm.mes_final_260106.service;

import com.hm.mes_final_260106.entity.Bom;
import com.hm.mes_final_260106.entity.Material;
import com.hm.mes_final_260106.entity.ProductionLog;
import com.hm.mes_final_260106.entity.WorkOrder;
import com.hm.mes_final_260106.repository.BomRepository;
import com.hm.mes_final_260106.repository.MaterialRepository;
import com.hm.mes_final_260106.repository.ProductionLogRepository;
import com.hm.mes_final_260106.repository.WorkOrderRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProductionService {
    private final ProductionLogRepository logRepo;
    private final MaterialRepository matRepo;
    private final WorkOrderRepository orderRepo;
    private final BomRepository bomRepo;

    // 자재 입고
    @Transactional  // 원자성 부여
    public Material inboundMaterial (String code, String name, int amount) {
        Material material = matRepo.findByCode(code)  // 자재 번호가 없으면 새로운 자재를 생성
                .orElse(Material
                        .builder()
                        .code(code)
                        .name(name)
                        .currentStock(0)  // 수량이 0
                        .build());

        // 자재 수량을 업데이트
        material.setCurrentStock(material.getCurrentStock() + amount);  // 수량 업데이트
        return matRepo.save(material);  // insert or update와 동일
    }


    // 작업 지시 생성 : 작업 지시는 생성된 DB에 기록을 위한 부분이고, 설비나 대시보드에 표현 하는 정보는 아님
    @Transactional
    public WorkOrder createWorkOrder(String productCode, int targetQty) {
       WorkOrder order = WorkOrder.builder()
                .productCode(productCode)
                .targetQty(targetQty)
                .currentQty(0)
                .status("WAITING")
                .build();
       return orderRepo.save(order);
    }

    // 설비 작업 할당 (C#)
    @Transactional
    public WorkOrder assignWorkToMachine(String machineId) {
        // 1. 해당 설비가 이미 하고 있는 일이 있는지 확인
        return orderRepo.findByStatusAndAssignedMachineId("IN_PROGRESS", machineId)
                .orElseGet(() -> {
                    // 2. 없다면 'WAITING' 상태인 가장 오랜된 지시를 하나 가져 옴
                    WorkOrder waiting = orderRepo.findFirstByStatusOrderByIdAsc("WAITING").orElse(null);
                    if (waiting != null) {
                      waiting.setStatus("IN_PROGRESS");
                      waiting.setAssignedMachineId(machineId);
                      return orderRepo.save(waiting);  // save()를 명시하지 않아도 변경 감지로 인해 업데이트 됨
                    }
                    return waiting;
                });
    }

    // 생산 실적 보고 (MES의 핵심: 실적 기록 + 자재 차감 + 수량증가) : 설비 -> Backend
    @Transactional
    public void reportProduction(Long orderId, String machineId, String result, String defectCode) {
        // 1. 지시 정보 확인
        WorkOrder order = orderRepo.findById(orderId)
                .orElseThrow(() -> new RuntimeException("작업 지시를 찾을 수 없습니다 ID: " + orderId));

        if ("COMPLETED".equals(order.getStatus())) return;

        // 2. 생산 이력(ProductionLog) 저장 : 5M1E 데이터 수집
        String serialNo = generateSerial(order.getProductCode());
        logRepo.save(ProductionLog.builder()
                .workOrderNo("WO-" + order.getId())
                .productCode(order.getProductCode())
                .machineId(machineId)
                .serialNo(serialNo)
                .result(result)
                .defectCode("NG".equals(result) ? defectCode : null)  // 결과가 성공이면 불량 코드에 null
                .productAt(LocalDateTime.now())
                .build());

        // 3. 자재 차감 (Backflushing) - 양품일 때만 자재를 차감
        if ("OK".equals(result)) {
            List<Bom> boms = bomRepo.findAllByProductCode(order.getProductCode());
            for (Bom bom : boms) {
                Material mat = bom.getMaterial();
                mat.setCurrentStock(mat.getCurrentStock() - bom.getRequiredQty());
            }
        }

        // 수량 업데이트 및 완료 처리
        order.setCurrentQty(order.getCurrentQty() + 1); // 생산 수량 업데이트
        if (order.getCurrentQty() >= order.getTargetQty()) order.setStatus("COMPLETED");

        log.info("[생산 보고] {} - 수량: {}/{}", order.getProductCode(), order.getCurrentQty(), order.getTargetQty());
    }

    // 시리얼 번호 생성 유틸리티
    private String generateSerial(String productCode) {
        return productCode + "-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }

    // 작업 지시 전체 목록 조회
    public List<WorkOrder> getAllWorkOrders(){
        return orderRepo.findAllByOrderByIdDesc();
    }


    // 전제 자재 재고량
    public List<Material> getMaterialStock(){
        return matRepo.findAll();
    }

}
