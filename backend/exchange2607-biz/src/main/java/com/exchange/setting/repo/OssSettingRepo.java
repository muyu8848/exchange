package com.exchange.setting.repo;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import com.exchange.setting.domain.OssSetting;

public interface OssSettingRepo extends JpaRepository<OssSetting, String>, JpaSpecificationExecutor<OssSetting> {

	OssSetting findTopByOrderByLatelyUpdateTime();

}
