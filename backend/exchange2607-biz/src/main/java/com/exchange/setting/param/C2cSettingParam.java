package com.exchange.setting.param;

import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.NotNull;

import lombok.Data;

@Data
public class C2cSettingParam {

	@NotNull
	@DecimalMin(value = "0", inclusive = false)
	private Integer tradeDeadlineMin;

	@NotNull
	@DecimalMin(value = "0", inclusive = false)
	private Integer tradeDeadlineMax;

	@NotNull
	@DecimalMin(value = "0", inclusive = false)
	private Integer autoReleaseMinutes;

	@NotNull
	@DecimalMin(value = "0", inclusive = false)
	private Integer maxActivePreOrders;

	@NotNull
	@DecimalMin(value = "0", inclusive = false)
	private Integer perOrderMaxPaymentAccounts;

	@NotNull
	@DecimalMin(value = "1", inclusive = true)
	private Integer protocolCancelHours;
	
	@NotNull
	@DecimalMin(value = "0", inclusive = false)
	private Integer appealIntervalMinutes;

}
