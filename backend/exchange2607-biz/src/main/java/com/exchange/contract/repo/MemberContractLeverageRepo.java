package com.exchange.contract.repo;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import com.exchange.contract.domain.MemberContractLeverage;

public interface MemberContractLeverageRepo
		extends JpaRepository<MemberContractLeverage, String>, JpaSpecificationExecutor<MemberContractLeverage> {

	Optional<MemberContractLeverage> findByMemberAccountIdAndSymbolCode(String memberAccountId, String symbolCode);
}
