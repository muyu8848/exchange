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

import com.exchange.investment.domain.InvestmentHourlyInterestLog;

public interface InvestmentHourlyInterestLogRepo extends
		JpaRepository<InvestmentHourlyInterestLog, String>, JpaSpecificationExecutor<InvestmentHourlyInterestLog> {

	boolean existsByFlexibleAccountIdAndInterestDateAndInterestHour(String flexibleAccountId, LocalDate interestDate,
			Integer interestHour);

	List<InvestmentHourlyInterestLog> findByFlexibleAccountIdAndInterestDateOrderByInterestHourAsc(
			String flexibleAccountId, LocalDate interestDate);

	@Query("SELECT COALESCE(SUM(l.interestAmount), 0) FROM InvestmentHourlyInterestLog l WHERE l.flexibleAccountId = :flexibleAccountId AND l.interestDate = :interestDate")
	BigDecimal sumInterestAmountByFlexibleAccountIdAndInterestDate(@Param("flexibleAccountId") String flexibleAccountId,
			@Param("interestDate") LocalDate interestDate);

	List<InvestmentHourlyInterestLog> findByMemberAccountIdAndInterestDate(String memberAccountId,
			LocalDate interestDate);

	@Modifying
	@Query(nativeQuery = true, value = "delete from investment_hourly_interest_log where create_time >= ?1 and create_time <= ?2")
	Integer dataClean(LocalDateTime startTime, LocalDateTime endTime);

	/** 汇总指定日期的已折算 USDT 小时利息 */
	@Query("SELECT COALESCE(SUM(l.interestUsdtAmount), 0) FROM InvestmentHourlyInterestLog l "
			+ "WHERE l.memberAccountId = :memberAccountId AND l.interestDate = :date")
	BigDecimal sumHourlyInterestUsdtByDate(@Param("memberAccountId") String memberAccountId,
			@Param("date") LocalDate date);

	List<InvestmentHourlyInterestLog> findByUsdtConvertStatusOrderByCreateTimeAsc(String usdtConvertStatus);

}
