package com.exchange.log.repo;

import java.time.LocalDateTime;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import com.exchange.log.domain.MemberAssetChangeLog;

public interface MemberAssetChangeLogRepo
		extends JpaRepository<MemberAssetChangeLog, String>, JpaSpecificationExecutor<MemberAssetChangeLog> {

	@Modifying
	@Query(nativeQuery = true, value = "delete from member_asset_change_log where change_time >= ?1 and change_time <= ?2")
	Integer dataClean(LocalDateTime startTime, LocalDateTime endTime);
	
}
