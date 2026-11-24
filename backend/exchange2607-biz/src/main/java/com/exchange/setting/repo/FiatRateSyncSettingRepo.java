package com.exchange.setting.repo;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import com.exchange.setting.domain.FiatRateSyncSetting;

public interface FiatRateSyncSettingRepo
		extends JpaRepository<FiatRateSyncSetting, String>, JpaSpecificationExecutor<FiatRateSyncSetting> {

	FiatRateSyncSetting findTopByOrderByLatelyUpdateTime();

}
