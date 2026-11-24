package com.exchange.withdraw.repo;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import com.exchange.withdraw.domain.WithdrawOrder;

public interface WithdrawOrderRepo
		extends JpaRepository<WithdrawOrder, String>, JpaSpecificationExecutor<WithdrawOrder> {

	long countByStatus(String status);

	long countByChainTypeAndAssetCodeAndStatusIn(String chainType, String assetCode, Collection<String> statuses);

	List<WithdrawOrder> findByStatusAndUsdtConvertStatus(String status, String usdtConvertStatus, Pageable pageable);

	@Modifying
	@Query(nativeQuery = true, value = "delete from withdraw_order where create_time >= ?1 and create_time <= ?2")
	Integer dataClean(LocalDateTime startTime, LocalDateTime endTime);

}
