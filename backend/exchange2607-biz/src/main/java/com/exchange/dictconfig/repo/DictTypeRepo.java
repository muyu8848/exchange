package com.exchange.dictconfig.repo;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import com.exchange.dictconfig.domain.DictType;

public interface DictTypeRepo extends JpaRepository<DictType, String>, JpaSpecificationExecutor<DictType> {

	DictType findByDictTypeCode(String dictTypeCode);

}
