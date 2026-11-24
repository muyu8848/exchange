package com.exchange.setting.param;

import java.math.BigDecimal;

import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.NotNull;

import lombok.Data;

@Data
public class MemberTransferSettingParam {

	@NotNull
	private Boolean transferFun;

	@NotNull
	@DecimalMin(value = "0.01")
	private BigDecimal minAmount;

	@NotNull
	@DecimalMin(value = "0.01")
	private BigDecimal maxAmount;

	@NotNull
	@DecimalMin(value = "0")
	private BigDecimal dailyAmountLimit;

	@NotNull
	@DecimalMin(value = "0")
	private Integer dailyCountLimit;

	@NotNull
	@DecimalMin(value = "0")
	private Double feeRate;

}
