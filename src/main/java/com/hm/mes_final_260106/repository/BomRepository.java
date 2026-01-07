package com.hm.mes_final_260106.repository;

import com.hm.mes_final_260106.entity.Bom;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BomRepository extends JpaRepository<Bom, Long> {
    List<Bom> findAllByProductCode(String productCode);  // 제품 생산에 소요되는 BOM (자재 명세서)

}
