package com.exchange.contract.repo;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import com.exchange.contract.domain.SecondContractPeriodConfig;

public interface SecondContractPeriodConfigRepo extends JpaRepository<SecondContractPeriodConfig, String>,
		JpaSpecificationExecutor<SecondContractPeriodConfig> {

	List<SecondContractPeriodConfig> findBySymbolCodeOrderByPeriodAsc(String symbolCode);

	SecondContractPeriodConfig findTopBySymbolCodeAndPeriod(String symbolCode, Integer period);

}
