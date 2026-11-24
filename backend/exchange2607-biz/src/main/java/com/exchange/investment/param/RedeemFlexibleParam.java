package com.exchange.investment.param;

import java.math.BigDecimal;

import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.Digits;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

import com.exchange.constants.Constant;

import lombok.Data;

@Data
public class RedeemFlexibleParam {

	@NotBlank
	private String memberAccountId;

	@NotBlank
	private String assetCode;

	@NotNull
	@DecimalMin(value = "0", inclusive = false)
	@Digits(integer = Constant.整数部分位数, fraction = Constant.小数部分位数)
	private BigDecimal amount;

}
