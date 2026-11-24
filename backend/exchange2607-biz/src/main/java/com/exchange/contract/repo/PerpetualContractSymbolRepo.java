package com.exchange.contract.repo;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import com.exchange.contract.domain.PerpetualContractSymbol;

public interface PerpetualContractSymbolRepo
		extends JpaRepository<PerpetualContractSymbol, String>, JpaSpecificationExecutor<PerpetualContractSymbol> {

	Optional<PerpetualContractSymbol> findTopBySymbolCodeAndDeletedFlagFalse(String symbolCode);

	/** 指定状态的未删除交易对（资金费结算等批量场景用） */
	List<PerpetualContractSymbol> findByStatusAndDeletedFlagIsFalse(String status);

}
