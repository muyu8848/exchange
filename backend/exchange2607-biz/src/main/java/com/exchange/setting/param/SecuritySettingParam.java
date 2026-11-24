package com.exchange.setting.param;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

import lombok.Data;

@Data
public class SecuritySettingParam {

	/** 会员最大在线端数 */
	@NotNull
	@Min(1)
	@Max(20)
	private Integer memberMaxLoginCount;

	/** 账号连续登录失败上限 */
	@NotNull
	@Min(1)
	@Max(100)
	private Integer accountLoginFailMaxAttempts;

	/** 账号登录锁定时长（分钟） */
	@NotNull
	@Min(1)
	@Max(1440)
	private Integer accountLoginFailLockMinutes;

	/** 同一IP连续登录失败上限 */
	@NotNull
	@Min(1)
	@Max(1000)
	private Integer ipLoginFailMaxAttempts;

	/** IP登录锁定时长（分钟） */
	@NotNull
	@Min(1)
	@Max(1440)
	private Integer ipLoginFailLockMinutes;
}
