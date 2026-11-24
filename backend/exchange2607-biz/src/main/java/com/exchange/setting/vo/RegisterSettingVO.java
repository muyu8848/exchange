package com.exchange.setting.vo;

import org.springframework.beans.BeanUtils;

import com.exchange.setting.domain.RegisterSetting;

import lombok.Data;

@Data
public class RegisterSettingVO {

	private Boolean inviteCodeRequired;

	public static RegisterSettingVO convertFor(RegisterSetting po) {
		RegisterSettingVO vo = new RegisterSettingVO();
		if (po != null) {
			BeanUtils.copyProperties(po, vo);
		}
		return vo;
	}

}
