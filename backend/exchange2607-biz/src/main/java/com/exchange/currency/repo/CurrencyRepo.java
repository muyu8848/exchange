package com.exchange.currency.repo;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import com.exchange.currency.domain.Currency;

public interface CurrencyRepo extends JpaRepository<Currency, String>, JpaSpecificationExecutor<Currency> {

	Currency findTopByCurrencyCodeAndDeletedFlagFalse(String currencyCode);

	List<Currency> findByDeletedFlagFalse();

	List<Currency> findByCurrencyCodeInAndDeletedFlagFalse(List<String> currencyCodes);

}
