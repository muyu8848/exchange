package com.exchange.setting.repo;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import com.exchange.setting.domain.RebateSetting;

public interface RebateSettingRepo
		extends JpaRepository<RebateSetting, String>, JpaSpecificationExecutor<RebateSetting> {

	RebateSetting findTopByOrderByLatelyUpdateTime();
}
