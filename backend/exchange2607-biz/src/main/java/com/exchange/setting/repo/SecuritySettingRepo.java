package com.exchange.setting.repo;

import org.springframework.data.jpa.repository.JpaRepository;

import com.exchange.setting.domain.SecuritySetting;

public interface SecuritySettingRepo extends JpaRepository<SecuritySetting, String> {

	SecuritySetting findTopByOrderByLatelyUpdateTime();

}
