package com.exchange.investment.param;

import java.math.BigDecimal;

import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.Digits;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

import com.exchange.constants.Constant;

import lombok.Data;

@Data
public class InvestmentEstimateParam {

	@NotBlank
	private String productId;

	@NotNull
	@DecimalMin(value = "0", inclusive = false)
	@Digits(integer = Constant.整数部分位数, fraction = Constant.小数部分位数)
	private BigDecimal amount;

	/** 当前登录用户ID，由 controller 透传，不参与前端入参校验 */
	private String memberAccountId;

}
