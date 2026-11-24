package com.exchange.setting.repo;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import com.exchange.setting.domain.C2cSetting;

public interface C2cSettingRepo extends JpaRepository<C2cSetting, String>, JpaSpecificationExecutor<C2cSetting> {

	C2cSetting findTopByOrderByLatelyUpdateTime();
	
}
