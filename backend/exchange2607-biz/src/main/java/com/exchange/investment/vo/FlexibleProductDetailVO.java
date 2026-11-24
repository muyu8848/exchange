package com.exchange.investment.vo;

import java.math.BigDecimal;

import com.exchange.common.json.BigDecimalStripTrailingZerosToPlainString;
import com.exchange.common.json.BigDecimalToPlainString;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import lombok.Data;

@Data
public class FlexibleProductDetailVO {

	private String assetCode;

	private String productId;

	private String flexibleAccountId;

	@JsonSerialize(using = BigDecimalToPlainString.class)
	private BigDecimal totalPrincipal;

	@JsonSerialize(using = BigDecimalToPlainString.class)
	private BigDecimal todayInterest;

	@JsonSerialize(using = BigDecimalToPlainString.class)
	private BigDecimal redeemableAmount;

	@JsonSerialize(using = BigDecimalToPlainString.class)
	private BigDecimal totalAccruedInterest;

	@JsonSerialize(using = BigDecimalToPlainString.class)
	private BigDecimal yesterdayInterest;

	@JsonSerialize(using = BigDecimalToPlainString.class)
	private BigDecimal annualRate;

	@JsonSerialize(using = BigDecimalToPlainString.class)
	private BigDecimal subsidizedRate;

	@JsonSerialize(using = BigDecimalStripTrailingZerosToPlainString.class)
	private BigDecimal rewardLimit;

	@JsonSerialize(using = BigDecimalToPlainString.class)
	private BigDecimal comprehensiveRate;

	private boolean productEnabled;

}
