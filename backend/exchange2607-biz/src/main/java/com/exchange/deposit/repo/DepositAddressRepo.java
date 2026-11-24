package com.exchange.deposit.repo;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import com.exchange.deposit.domain.DepositAddress;

public interface DepositAddressRepo
		extends JpaRepository<DepositAddress, String>, JpaSpecificationExecutor<DepositAddress> {

	DepositAddress findTopByAddressAndDeletedFlagIsFalse(String address);

	long countByChainTypeAndStatusAndDeletedFlagIsFalse(String chainType, String status);

	DepositAddress findTopByCurrentOrderIdAndDeletedFlagIsFalse(String currentOrderId);

	DepositAddress findTopByChainTypeAndAddressAndDeletedFlagIsFalse(String chainType, String address);

}
