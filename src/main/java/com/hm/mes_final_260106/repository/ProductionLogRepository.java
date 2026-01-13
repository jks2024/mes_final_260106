package com.hm.mes_final_260106.repository;

import com.hm.mes_final_260106.entity.ProductionLog;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ProductionLogRepository extends JpaRepository<ProductionLog, Long> {
    // 최근 생산된 순으로 15건 조회
    List<ProductionLog> findTop15ByOrderByIdDesc();
}
