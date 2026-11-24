package com.exchange.investment.repo;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import com.exchange.investment.domain.InvestmentFlexiblePosition;

public interface InvestmentFlexiblePositionRepo extends JpaRepository<InvestmentFlexiblePosition, String>,
		JpaSpecificationExecutor<InvestmentFlexiblePosition> {

	@Modifying
	@Query(nativeQuery = true, value = "delete from investment_flexible_position where invest_time >= ?1 and invest_time <= ?2")
	Integer dataClean(LocalDateTime startTime, LocalDateTime endTime);

	List<InvestmentFlexiblePosition> findByMemberAccountIdAndStatus(String memberAccountId, String status);

	List<InvestmentFlexiblePosition> findByMemberAccountIdAndAssetCodeAndStatus(String memberAccountId,
			String assetCode, String status);

	/** 账户维度：待赎回的持仓记录（按申购时间 FIFO） */
	List<InvestmentFlexiblePosition> findByFlexibleAccountIdAndStatusOrderByInvestTimeAsc(String flexibleAccountId,
			String status);

	/** 账户维度：本期到计息时间的持仓记录（按申购时间 FIFO） */
	List<InvestmentFlexiblePosition> findByFlexibleAccountIdAndStatusAndNextAccrualTimeLessThanEqualAndPrincipalGreaterThanOrderByInvestTimeAsc(
			String flexibleAccountId, String status, LocalDateTime nextAccrualTime, BigDecimal principal);

	long countByMemberAccountId(String memberAccountId);

}
