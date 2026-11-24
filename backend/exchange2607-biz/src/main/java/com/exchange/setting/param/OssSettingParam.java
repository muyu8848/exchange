package com.exchange.setting.param;

import javax.validation.constraints.NotBlank;

import lombok.Data;

@Data
public class OssSettingParam {

	@NotBlank
	private String type;

	@NotBlank
	private String localStoragePath;

	@NotBlank
	private String publicFetchUrl;

}
