package com.exchange.verificationcode.repo;

import java.time.LocalDateTime;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import com.exchange.verificationcode.domain.VerificationCodeSendRecord;

public interface VerificationCodeSendRecordRepo
		extends JpaRepository<VerificationCodeSendRecord, String>, JpaSpecificationExecutor<VerificationCodeSendRecord> {

	@Modifying
	@Query(nativeQuery = true, value = "delete from verification_code_send_record where create_time >= ?1 and create_time <= ?2")
	Integer dataClean(LocalDateTime startTime, LocalDateTime endTime);

}
