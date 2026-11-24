package com.exchange.dashboard.vo;

import java.math.BigDecimal;

import com.exchange.common.json.BigDecimalStripTrailingZerosToPlainString;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import lombok.Data;

@Data
public class PerpetualDashboardDailyItemVO {

	private String date;

	private Long orderCount = 0L;

	@JsonSerialize(using = BigDecimalStripTrailingZerosToPlainString.class)
	private BigDecimal dealAmount = BigDecimal.ZERO;

	@JsonSerialize(using = BigDecimalStripTrailingZerosToPlainString.class)
	private BigDecimal feeUsdtAmount = BigDecimal.ZERO;

	@JsonSerialize(using = BigDecimalStripTrailingZerosToPlainString.class)
	private BigDecimal realizedPnl = BigDecimal.ZERO;

	private Long liquidateCount = 0L;

	@JsonSerialize(using = BigDecimalStripTrailingZerosToPlainString.class)
	private BigDecimal fundingNetAmount = BigDecimal.ZERO;
}
