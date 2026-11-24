package com.exchange.currency.repo;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import com.exchange.currency.domain.ChainNetwork;

public interface ChainNetworkRepo
		extends JpaRepository<ChainNetwork, String>, JpaSpecificationExecutor<ChainNetwork> {

	ChainNetwork findTopByChainTypeAndDeletedFlagIsFalse(String chainType);

}
