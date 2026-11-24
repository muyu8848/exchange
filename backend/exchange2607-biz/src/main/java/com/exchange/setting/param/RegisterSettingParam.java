package com.exchange.setting.param;

import javax.validation.constraints.NotNull;

import lombok.Data;

@Data
public class RegisterSettingParam {

	@NotNull
	private Boolean inviteCodeRequired;

}
