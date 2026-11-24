package com.exchange.member.param;

import javax.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class SetFundPwdParam {

	@NotBlank
	private String newPwd;

	@NotBlank
	private String contactType;

	@NotBlank
	private String verifyCode;

	/** 会员ID，Controller 层注入 */
	private String memberAccountId;
}
