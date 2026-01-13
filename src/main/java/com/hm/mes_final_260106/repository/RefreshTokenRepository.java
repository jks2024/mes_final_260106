package com.hm.mes_final_260106.repository;

import com.hm.mes_final_260106.entity.Member;
import com.hm.mes_final_260106.entity.RefreshToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {
    Optional<RefreshToken> findByMember(Member member);
    Optional<RefreshToken> findByMemberId(Long memberId);
}
