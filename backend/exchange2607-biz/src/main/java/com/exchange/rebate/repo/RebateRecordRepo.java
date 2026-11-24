package com.exchange.rebate.repo;

import java.time.LocalDateTime;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import com.exchange.rebate.domain.RebateRecord;

public interface RebateRecordRepo extends JpaRepository<RebateRecord, String>, JpaSpecificationExecutor<RebateRecord> {

	boolean existsBySourceOrderIdAndBizType(String sourceOrderId, String bizType);

	@Modifying
	@Query(nativeQuery = true, value = "delete from rebate_record where create_time >= ?1 and create_time <= ?2")
	Integer dataClean(LocalDateTime startTime, LocalDateTime endTime);

}
