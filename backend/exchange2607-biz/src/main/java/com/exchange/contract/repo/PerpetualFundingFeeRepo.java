package com.exchange.contract.repo;

import java.time.LocalDateTime;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import com.exchange.contract.domain.PerpetualFundingFee;

public interface PerpetualFundingFeeRepo
		extends JpaRepository<PerpetualFundingFee, String>, JpaSpecificationExecutor<PerpetualFundingFee> {

	@Modifying
	@Query(nativeQuery = true, value = "delete from perpetual_funding_fee where create_time >= ?1 and create_time <= ?2")
	Integer dataClean(LocalDateTime startTime, LocalDateTime endTime);

}
