package com.exchange.setting.param;

import javax.validation.constraints.NotBlank;

import lombok.Data;

@Data
public class SystemSettingParam {

	@NotBlank
	private String appDownloadUrl;

	@NotBlank
	private String appSchema;

	@NotBlank
	private String h5Url;
	
	private String customerServiceUrl;

}
