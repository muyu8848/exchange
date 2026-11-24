package com.exchange.setting.param;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

import lombok.Data;

@Data
public class FiatRateSyncSettingParam {

	@NotBlank
	private String exchangeRateSource;

	@NotNull
	private Boolean autoSyncExchangeRate;

}
