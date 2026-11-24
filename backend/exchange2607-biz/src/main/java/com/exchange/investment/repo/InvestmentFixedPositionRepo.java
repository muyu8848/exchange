package com.exchange.investment.repo;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.exchange.investment.domain.InvestmentFixedPosition;

public interface InvestmentFixedPositionRepo
		extends JpaRepository<InvestmentFixedPosition, String>, JpaSpecificationExecutor<InvestmentFixedPosition> {

	@Modifying
	@Query(nativeQuery = true, value = "delete from investment_fixed_position where invest_time >= ?1 and invest_time <= ?2")
	Integer dataClean(LocalDateTime startTime, LocalDateTime endTime);

	List<InvestmentFixedPosition> findTop500ByStatusAndNextAccrualTimeLessThanEqualOrderByNextAccrualTimeAsc(
			String status, LocalDateTime nextAccrualTime);

	List<InvestmentFixedPosition> findByMemberAccountIdAndStatus(String memberAccountId, String status);

	/** 会员全部定期持仓（含已赎回，供累计收益统计） */
	List<InvestmentFixedPosition> findByMemberAccountId(String memberAccountId);

	List<InvestmentFixedPosition> findByMemberAccountIdAndAssetCodeAndStatus(String memberAccountId, String assetCode,
			String status);

	@Query("SELECT COALESCE(SUM(p.principal), 0) FROM InvestmentFixedPosition p WHERE p.memberAccountId = :memberAccountId AND p.productId = :productId AND p.status = :status")
	BigDecimal sumPrincipalByMemberAccountIdAndProductIdAndStatus(@Param("memberAccountId") String memberAccountId,
			@Param("productId") String productId, @Param("status") String status);

	long countByMemberAccountId(String memberAccountId);

}
