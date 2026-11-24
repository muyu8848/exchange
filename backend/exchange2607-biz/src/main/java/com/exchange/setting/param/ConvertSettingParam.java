package com.exchange.setting.param;

import java.math.BigDecimal;

import javax.validation.constraints.NotNull;

import lombok.Data;

@Data
public class ConvertSettingParam {

	@NotNull
	private Boolean enabled;

	@NotNull
	private BigDecimal spread;

	@NotNull
	private BigDecimal minAmount;

	@NotNull
	private BigDecimal maxAmount;

	@NotNull
	private Integer quoteTtlSeconds;
}
