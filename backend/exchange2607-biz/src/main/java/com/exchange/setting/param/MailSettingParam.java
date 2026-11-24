package com.exchange.setting.param;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;

import lombok.Data;

@Data
public class MailSettingParam {

	@NotBlank
	private String host;

	@NotNull
	private Integer port;

	@NotNull
	private Boolean needAuthFlag;

	@NotBlank
	private String mailAccount;

	@NotBlank
	private String mailUser;

	@NotBlank
	private String mailPwd;

	@NotBlank
	@Pattern(regexp = "SSL|STARTTLS|NONE")
	private String encryptionType;

}
