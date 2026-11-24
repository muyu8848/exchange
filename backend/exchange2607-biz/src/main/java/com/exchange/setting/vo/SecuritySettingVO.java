package com.exchange.setting.vo;

import org.springframework.beans.BeanUtils;

import com.exchange.setting.domain.SecuritySetting;

import lombok.Data;

@Data
public class SecuritySettingVO {

	/** 会员最大在线端数 */
	private Integer memberMaxLoginCount;

	/** 账号连续登录失败上限 */
	private Integer accountLoginFailMaxAttempts;

	/** 账号登录锁定时长（分钟） */
	private Integer accountLoginFailLockMinutes;

	/** 同一IP连续登录失败上限 */
	private Integer ipLoginFailMaxAttempts;

	/** IP登录锁定时长（分钟） */
	private Integer ipLoginFailLockMinutes;

	public static SecuritySettingVO convertFor(SecuritySetting setting) {
		SecuritySettingVO vo = new SecuritySettingVO();
		if (setting != null) {
			BeanUtils.copyProperties(setting, vo);
		}
		return vo;
	}
}
