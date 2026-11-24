package com.exchange.member.param;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

import lombok.Data;

@Data
public class MemberAssetOpenAccountParam {

	@NotNull
	private Long memberUid;

	@NotBlank
	private String accountType;

	@NotBlank
	private String assetCode;

}
