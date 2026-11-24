package com.exchange.log.repo;

import java.time.LocalDateTime;

import com.exchange.log.domain.MemberContactBindLog;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

public interface MemberContactBindLogRepo
		extends JpaRepository<MemberContactBindLog, String>, JpaSpecificationExecutor<MemberContactBindLog> {

	@Modifying
	@Query(nativeQuery = true, value = "delete from member_contact_bind_log where create_time >= ?1 and create_time <= ?2")
	Integer dataClean(LocalDateTime startTime, LocalDateTime endTime);

}
