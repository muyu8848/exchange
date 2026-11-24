package com.exchange.investment.repo;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import com.exchange.investment.domain.InvestmentRecord;

public interface InvestmentRecordRepo
		extends JpaRepository<InvestmentRecord, String>, JpaSpecificationExecutor<InvestmentRecord> {

	@Modifying
	@Query(nativeQuery = true, value = "delete from investment_record where create_time >= ?1 and create_time <= ?2")
	Integer dataClean(LocalDateTime startTime, LocalDateTime endTime);

	List<InvestmentRecord> findByUsdtConvertStatusOrderByCreateTimeAsc(String usdtConvertStatus);

}
