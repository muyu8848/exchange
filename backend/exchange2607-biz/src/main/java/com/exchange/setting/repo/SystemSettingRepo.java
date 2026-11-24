package com.exchange.setting.repo;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import com.exchange.setting.domain.SystemSetting;

public interface SystemSettingRepo
		extends JpaRepository<SystemSetting, String>, JpaSpecificationExecutor<SystemSetting> {

	SystemSetting findTopByOrderByLatelyUpdateTime();

}
