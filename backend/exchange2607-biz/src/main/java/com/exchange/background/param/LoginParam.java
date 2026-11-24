package com.exchange.background.param;

import javax.validation.constraints.NotBlank;

import lombok.Data;

@Data
public class LoginParam {

	@NotBlank
	private String userName;

	@NotBlank
	private String loginPwd;
	
	private String googleVerCode;

	/** 客户端 IP，由 Controller 填充 */
	@NotBlank
	private String ip;

}
