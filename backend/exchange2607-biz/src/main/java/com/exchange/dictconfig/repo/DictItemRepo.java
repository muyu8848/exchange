package com.exchange.dictconfig.repo;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import com.exchange.dictconfig.domain.DictItem;

public interface DictItemRepo extends JpaRepository<DictItem, String>, JpaSpecificationExecutor<DictItem> {

	DictItem findByDictTypeDictTypeCodeAndDictItemCode(String dictTypeCode, String dictItemCode);

	List<DictItem> findByDictTypeDictTypeCodeOrderBySortNum(String dictTypeCode);
	
	List<DictItem> findByDictTypeIdOrderBySortNum(String dictTypeId);

}
