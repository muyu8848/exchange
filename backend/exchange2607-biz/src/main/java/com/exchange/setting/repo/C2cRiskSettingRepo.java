package com.exchange.setting.repo;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import com.exchange.setting.domain.C2cRiskSetting;

public interface C2cRiskSettingRepo
		extends JpaRepository<C2cRiskSetting, String>, JpaSpecificationExecutor<C2cRiskSetting> {

	C2cRiskSetting findTopByOrderByLatelyUpdateTime();

}
