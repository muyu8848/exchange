package com.exchange.member.param;

import javax.validation.constraints.NotBlank;

import lombok.Data;

@Data
public class UpdateFundPwdParam {

	@NotBlank
	private String oldPwd;

	@NotBlank
	private String newPwd;

	@NotBlank
	private String memberAccountId;

}
