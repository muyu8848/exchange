package com.exchange.setting.repo;

import org.springframework.data.jpa.repository.JpaRepository;

import com.exchange.setting.domain.ConvertSetting;

public interface ConvertSettingRepo extends JpaRepository<ConvertSetting, String> {

	ConvertSetting findTopByOrderByLatelyUpdateTime();
}
