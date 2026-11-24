package com.exchange.contract.repo;

import java.time.LocalDateTime;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import com.exchange.contract.domain.PerpetualFundingRateHistory;

public interface PerpetualFundingRateHistoryRepo
		extends JpaRepository<PerpetualFundingRateHistory, String>,
		JpaSpecificationExecutor<PerpetualFundingRateHistory> {

	/** 某交易对最近一次结算记录（用于判断是否到结算时间） */
	Optional<PerpetualFundingRateHistory> findTopBySymbolCodeOrderByFundingTimeDesc(String symbolCode);

	@Modifying
	@Query(nativeQuery = true, value = "delete from perpetual_funding_rate_history where create_time >= ?1 and create_time <= ?2")
	Integer dataClean(LocalDateTime startTime, LocalDateTime endTime);

}
