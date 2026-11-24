package com.exchange.withdraw.repo;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import com.exchange.withdraw.domain.WithdrawChannel;

public interface WithdrawChannelRepo
		extends JpaRepository<WithdrawChannel, String>, JpaSpecificationExecutor<WithdrawChannel> {

	WithdrawChannel findTopByChainTypeAndAssetCodeAndDeletedFlagIsFalse(String chainType, String assetCode);

}
