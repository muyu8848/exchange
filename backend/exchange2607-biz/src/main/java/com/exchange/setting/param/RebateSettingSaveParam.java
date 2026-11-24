package com.exchange.setting.param;

import java.math.BigDecimal;

import javax.validation.constraints.DecimalMax;
import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.NotNull;

import lombok.Data;

@Data
public class RebateSettingSaveParam {

	@NotNull
	@DecimalMin(value = "0", inclusive = true)
	@DecimalMax(value = "1", inclusive = true)
	private BigDecimal rate;
}
