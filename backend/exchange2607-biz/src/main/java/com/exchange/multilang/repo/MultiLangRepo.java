package com.exchange.multilang.repo;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import com.exchange.multilang.domain.MultiLang;

public interface MultiLangRepo extends JpaRepository<MultiLang, String>, JpaSpecificationExecutor<MultiLang> {

	List<MultiLang> findByStatusOrderBySortNumAsc(String status);

	List<MultiLang> findAllByOrderBySortNumAsc();

	MultiLang findByLangCode(String langCode);

	long countByStatus(String status);

}
