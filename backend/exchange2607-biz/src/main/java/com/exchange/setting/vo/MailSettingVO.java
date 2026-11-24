package com.exchange.setting.vo;

import org.springframework.beans.BeanUtils;

import com.exchange.setting.domain.MailSetting;

import lombok.Data;

@Data
public class MailSettingVO {

	private String host;

	private Integer port;

	private Boolean needAuthFlag;

	private String mailAccount;

	private String mailUser;

	private String mailPwd;

	private String encryptionType;

	public static MailSettingVO convertFor(MailSetting po) {
		MailSettingVO vo = new MailSettingVO();
		if (po != null) {
			BeanUtils.copyProperties(po, vo);
		}
		return vo;
	}

}
