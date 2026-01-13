package com.hm.mes_final_260106.service;

import com.hm.mes_final_260106.dto.RecentLogDto;
import com.hm.mes_final_260106.entity.*;
import com.hm.mes_final_260106.exception.CustomException;
import com.hm.mes_final_260106.repository.*;
import com.hm.mes_final_260106.security.SecurityUtil;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProductionService {
    private final ProductionLogRepository logRepo;
    private final MaterialRepository matRepo;
    private final WorkOrderRepository orderRepo;
    private final BomRepository bomRepo;
    private final MemberRepository memberRepository;

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
                        // 자재가 있는지 먼저 확인
                        if (!isMaterialAvailable(waiting.getProductCode())) {
                            return null; // 자재가 없으면 할당하지 않음 (C#은 NoContent 응답을 받음)
                        }

                        waiting.setStatus("IN_PROGRESS");
                        waiting.setAssignedMachineId(machineId);
                        return orderRepo.save(waiting);  // save()를 명시하지 않아도 변경 감지로 인해 업데이트 됨
                    }
                    return waiting;
                });
    }

    // 생산 실적 보고 (MES의 핵심: 실적 기록 + 자재 차감 + 수량증가) : 설비 -> Backend
    @Transactional
    public void reportProduction(Long orderId, String machineId, String result, String defectCode, String serialNo) {
        // 1. SecurityContext에서 현재 로그인한 작업자 정보 확보 (매개변수 email 삭제)
        Member operator = null;
        try {
            // 1. SecurityContext에서 현재 로그인한 작업자 ID 추출
            Long currentMemberId = SecurityUtil.getCurrentMemberId();
            operator = memberRepository.findById(currentMemberId).orElseThrow();
        } catch (Exception e) {
            log.warn("작업자 정보를 찾을 수 없어 기본 사용자로 처리합니다.");
        }

        WorkOrder order = orderRepo.findById(orderId)
                .orElseThrow(() -> new RuntimeException("작업 지시를 찾을 수 없습니다 ID: " + orderId));


        if ("COMPLETED".equals(order.getStatus())) return;

        // 2. 생산 이력(ProductionLog) 저장 : 5M1E 데이터 수집
        logRepo.save(ProductionLog.builder()
                .workOrder(order)
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
                int required = bom.getRequiredQty();
                int current = mat.getCurrentStock();

                // [핵심 추가] 차감 전 재고 확인
                if (current < required) {
                    // 자재가 부족하면 예외를 던져 전체 프로세스를 롤백시킵니다.
                    // 메시지에 부족한 자재명을 담아 설비나 UI에 알릴 수 있습니다.
                    throw new CustomException("SHORTAGE", "MATERIAL_SHORTAGE:" + mat.getName());
                }

                // 재고가 충분할 때만 차감 실행
                mat.setCurrentStock(current - required);
                log.info("[Backflushing] 자재: {}, 차감후 재고: {}", mat.getName(), mat.getCurrentStock());
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

    // 자재 현황 체크 로직
    private boolean isMaterialAvailable(String productCode) {
        List<Bom> boms = bomRepo.findAllByProductCode(productCode);
        for (Bom bom : boms) {
            // 현재 재고 < 1개 생산 당 소요량 이면 생산 불가
            if (bom.getMaterial().getCurrentStock() < bom.getRequiredQty()) {
                log.error("자재 부족: {} (현재: {}, 필요: {})",
                        bom.getMaterial().getName(), bom.getMaterial().getCurrentStock(), bom.getRequiredQty());
                return false;
            }
        }
        return true;
    }

    // 최근 생산된 순으로 상위 15건 조회
    public List<RecentLogDto> getRecentLogs() {
        return logRepo.findTop15ByOrderByIdDesc().stream()
                .map(log -> new RecentLogDto(
                        log.getId(),
                        log.getSerialNo(),
                        log.getOperator() != null ? log.getOperator().getName() : "시스템",
                        log.getResult(),
                        log.getMachineId(),
                        log.getProductAt()
                ))
                .collect(Collectors.toList());
    }

}
