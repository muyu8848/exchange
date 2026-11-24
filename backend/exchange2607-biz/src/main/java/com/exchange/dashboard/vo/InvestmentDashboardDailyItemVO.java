package com.exchange.dashboard.vo;

import java.math.BigDecimal;

import com.exchange.common.json.BigDecimalStripTrailingZerosToPlainString;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import lombok.Data;

@Data
public class InvestmentDashboardDailyItemVO {

	/** 时间点：yyyy-MM-dd */
	private String date;

	/** 申购笔数 */
	private Long subscribeCount = 0L;

	/** 申购金额（USDT） */
	@JsonSerialize(using = BigDecimalStripTrailingZerosToPlainString.class)
	private BigDecimal subscribeAmount = BigDecimal.ZERO;

	/** 赎回笔数 */
	private Long redeemCount = 0L;

	/** 赎回金额（USDT，含收益） */
	@JsonSerialize(using = BigDecimalStripTrailingZerosToPlainString.class)
	private BigDecimal redeemAmount = BigDecimal.ZERO;

	/** 利息发放（USDT） */
	@JsonSerialize(using = BigDecimalStripTrailingZerosToPlainString.class)
	private BigDecimal interestAmount = BigDecimal.ZERO;

	/** 净流入（USDT）= 申购 − 赎回 */
	@JsonSerialize(using = BigDecimalStripTrailingZerosToPlainString.class)
	private BigDecimal netInflow = BigDecimal.ZERO;
}
