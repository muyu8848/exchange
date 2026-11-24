package com.exchange.dashboard.vo;

import java.math.BigDecimal;

import com.exchange.common.json.BigDecimalStripTrailingZerosToPlainString;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import lombok.Data;

@Data
public class InvestmentDashboardTrendItemVO {

	/** 时间点：yyyy-MM-dd */
	private String date;

	@JsonSerialize(using = BigDecimalStripTrailingZerosToPlainString.class)
	private BigDecimal subscribeAmount = BigDecimal.ZERO;

	@JsonSerialize(using = BigDecimalStripTrailingZerosToPlainString.class)
	private BigDecimal redeemAmount = BigDecimal.ZERO;

	@JsonSerialize(using = BigDecimalStripTrailingZerosToPlainString.class)
	private BigDecimal interestAmount = BigDecimal.ZERO;
}
