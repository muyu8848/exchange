package com.exchange.setting.repo;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import com.exchange.setting.domain.RegisterSetting;

public interface RegisterSettingRepo
		extends JpaRepository<RegisterSetting, String>, JpaSpecificationExecutor<RegisterSetting> {

	RegisterSetting findTopByOrderByLatelyUpdateTime();

}
