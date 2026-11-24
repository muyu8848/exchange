package com.exchange.background.param;

import javax.validation.constraints.NotBlank;

import com.exchange.common.operlog.SensitiveParam;

import lombok.Data;

@Data
public class AddBackgroundAccountParam {

	@NotBlank
	private String userName;

	@NotBlank
	@SensitiveParam
	private String loginPwd;
	
}
