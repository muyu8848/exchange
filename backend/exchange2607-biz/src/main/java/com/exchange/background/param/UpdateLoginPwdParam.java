package com.exchange.background.param;

import javax.validation.constraints.NotBlank;

import com.exchange.common.operlog.SensitiveParam;

import lombok.Data;

@Data
public class UpdateLoginPwdParam {

	@NotBlank
	@SensitiveParam
	private String oldPwd;

	@NotBlank
	@SensitiveParam
	private String newPwd;

	private String id;

}
