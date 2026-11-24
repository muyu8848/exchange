package com.exchange.background.param;

import javax.validation.constraints.NotBlank;

import lombok.Data;

@Data
public class UpdateBackgroundAccountParam {

	@NotBlank
	private String id;

	@NotBlank
	private String userName;
	
	@NotBlank
	private String status;

}
