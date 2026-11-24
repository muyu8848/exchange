package com.exchange.member.repo;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import com.exchange.member.domain.CountryAreaCode;

public interface CountryAreaCodeRepo
		extends JpaRepository<CountryAreaCode, String>, JpaSpecificationExecutor<CountryAreaCode> {

	List<CountryAreaCode> findAllByOrderBySortNumAsc();

	List<CountryAreaCode> findByStatusOrderBySortNumAsc(String status);

}
