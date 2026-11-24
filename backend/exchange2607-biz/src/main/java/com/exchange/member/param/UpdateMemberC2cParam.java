package com.exchange.member.param;

import javax.validation.constraints.NotBlank;

import lombok.Data;

@Data
public class UpdateMemberC2cParam {

	@NotBlank
	private String id;

	@NotBlank
	private String c2cBuyStatus;

	@NotBlank
	private String c2cSellStatus;

}
