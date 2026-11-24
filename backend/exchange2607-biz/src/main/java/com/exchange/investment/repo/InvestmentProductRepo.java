package com.exchange.investment.repo;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.exchange.investment.domain.InvestmentProduct;

public interface InvestmentProductRepo
		extends JpaRepository<InvestmentProduct, String>, JpaSpecificationExecutor<InvestmentProduct> {

	Optional<InvestmentProduct> findByIdAndDeletedFlagFalse(String code);

	long countByAssetCodeAndProductTypeAndDeletedFlagFalse(String assetCode, String productType);

	long countByAssetCodeAndProductTypeAndLockDaysAndDeletedFlagFalse(String assetCode, String productType,
			Integer lockDays);

	@Modifying
	@Query(nativeQuery = true, value = "UPDATE investment_product SET remaining_quota = remaining_quota - :amount, lately_update_time = :now WHERE id = :id AND remaining_quota >= :amount")
	int deductRemainingQuota(@Param("id") String id, @Param("amount") BigDecimal amount, LocalDateTime now);

}
