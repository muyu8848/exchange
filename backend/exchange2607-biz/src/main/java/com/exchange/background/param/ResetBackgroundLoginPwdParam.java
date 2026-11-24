package com.exchange.background.param;

import javax.validation.constraints.NotBlank;

import com.exchange.common.operlog.SensitiveParam;

import lombok.Data;

/**
 * 后台重置其他后台账号登录密码入参（与「修改本人密码」的
 * {@link UpdateLoginPwdParam} 区分：后者需要校验旧密码）。
 */
@Data
public class ResetBackgroundLoginPwdParam {

	@NotBlank
	private String id;

	@NotBlank
	@SensitiveParam
	private String newPwd;

}
