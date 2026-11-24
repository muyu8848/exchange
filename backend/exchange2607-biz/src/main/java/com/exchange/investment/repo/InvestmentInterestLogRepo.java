package com.exchange.investment.repo;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.exchange.investment.domain.InvestmentInterestLog;

public interface InvestmentInterestLogRepo
		extends JpaRepository<InvestmentInterestLog, String>, JpaSpecificationExecutor<InvestmentInterestLog> {

	boolean existsByPositionIdAndInterestDate(String positionId, LocalDate interestDate);

	boolean existsByFlexibleAccountIdAndInterestDate(String flexibleAccountId, LocalDate interestDate);

	List<InvestmentInterestLog> findByFlexibleAccountIdAndInterestDate(String flexibleAccountId,
			LocalDate interestDate);

	@Query("SELECT COALESCE(SUM(l.interestAmount), 0) FROM InvestmentInterestLog l WHERE l.memberAccountId = :memberAccountId AND l.interestDate = :interestDate")
	BigDecimal sumInterestAmountBymemberAccountIdAndInterestDate(@Param("memberAccountId") String memberAccountId,
			@Param("interestDate") LocalDate interestDate);

	List<InvestmentInterestLog> findByMemberAccountIdAndInterestDate(String memberAccountId, LocalDate interestDate);

	List<InvestmentInterestLog> findByMemberAccountId(String memberAccountId);

	List<InvestmentInterestLog> findByMemberAccountIdAndAssetCodeAndProductTypeAndInterestDate(String memberAccountId,
			String assetCode, String productType, LocalDate interestDate);

	@Query("SELECT COALESCE(SUM(l.interestAmount), 0) FROM InvestmentInterestLog l WHERE l.flexibleAccountId = :flexibleAccountId AND l.interestType = :interestType AND l.interestDate = :interestDate")
	BigDecimal sumInterestAmountByFlexibleAccountIdAndInterestTypeAndInterestDate(
			@Param("flexibleAccountId") String flexibleAccountId, @Param("interestType") String interestType,
			@Param("interestDate") LocalDate interestDate);

	List<InvestmentInterestLog> findByFlexibleAccountIdAndInterestTypeOrderByInterestDateDesc(String flexibleAccountId,
			String interestType);

	@Modifying
	@Query(nativeQuery = true, value = "delete from investment_interest_log where create_time >= ?1 and create_time <= ?2")
	Integer dataClean(LocalDateTime startTime, LocalDateTime endTime);

	/** 按产品类型 + 收益类型汇总已折算 USDT 的利息（定期到期发放，不限日期） */
	@Query("SELECT COALESCE(SUM(l.interestUsdtAmount), 0) FROM InvestmentInterestLog l "
			+ "WHERE l.memberAccountId = :memberAccountId AND l.productType = :productType "
			+ "AND l.interestType = :interestType")
	BigDecimal sumInterestUsdtAmount(@Param("memberAccountId") String memberAccountId,
			@Param("productType") String productType, @Param("interestType") String interestType);

	/** 汇总指定日期之前的已折算 USDT 利息（活期日汇总，不含当天） */
	@Query("SELECT COALESCE(SUM(l.interestUsdtAmount), 0) FROM InvestmentInterestLog l "
			+ "WHERE l.memberAccountId = :memberAccountId AND l.productType = :productType "
			+ "AND l.interestType = :interestType AND l.interestDate < :date")
	BigDecimal sumInterestUsdtAmountBeforeDate(@Param("memberAccountId") String memberAccountId,
			@Param("productType") String productType, @Param("interestType") String interestType,
			@Param("date") LocalDate date);

	List<InvestmentInterestLog> findByUsdtConvertStatusOrderByCreateTimeAsc(String usdtConvertStatus);

	/**
	 * 按持仓时间区间级联清理利息流水（数据清理用）。
	 * <p>
	 * 定期按 position_id 关联 investment_fixed_position；活期按 flexible_account_id 关联 investment_flexible_account
	 * （活期账户以 create_time 为时间维度）。
	 */
	@Modifying
	@Query(nativeQuery = true, value = "delete from investment_interest_log where position_id in (select id from investment_fixed_position where invest_time >= ?1 and invest_time <= ?2) or flexible_account_id in (select id from investment_flexible_account where create_time >= ?1 and create_time <= ?2)")
	Integer dataCleanByPosition(LocalDateTime startTime, LocalDateTime endTime);

}
