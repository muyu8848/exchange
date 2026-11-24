package com.exchange.background.repo;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import com.exchange.background.domain.Menu;

public interface MenuRepo extends JpaRepository<Menu, String>, JpaSpecificationExecutor<Menu> {
	
	List<Menu> findByDeletedFlagFalseOrderBySortNum();
	
	List<Menu> findByParentIdAndDeletedFlagFalse(String parentId);
	
	List<Menu> findByIdInAndDeletedFlagFalseOrderBySortNum(List<String> ids);
	
	boolean existsByUrlAndTypeAndDeletedFlagFalse(String url, String type);

	boolean existsByIdAndTypeAndDeletedFlagFalse(String id, String type);

}
