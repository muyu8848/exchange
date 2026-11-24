package com.exchange.setting.repo;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import com.exchange.setting.domain.MailSetting;

public interface MailSettingRepo extends JpaRepository<MailSetting, String>, JpaSpecificationExecutor<MailSetting> {

	MailSetting findTopByOrderByLatelyUpdateTime();

}
