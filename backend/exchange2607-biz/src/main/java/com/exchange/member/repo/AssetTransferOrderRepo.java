package com.exchange.member.repo;

import java.time.LocalDateTime;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import com.exchange.member.domain.AssetTransferOrder;

public interface AssetTransferOrderRepo
		extends JpaRepository<AssetTransferOrder, String>, JpaSpecificationExecutor<AssetTransferOrder> {

	@Modifying
	@Query(nativeQuery = true, value = "delete from asset_transfer_order where create_time >= ?1 and create_time <= ?2")
	Integer dataClean(LocalDateTime startTime, LocalDateTime endTime);

}
