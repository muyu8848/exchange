package com.exchange.dashboard.vo;

import java.math.BigDecimal;

import com.exchange.common.json.BigDecimalStripTrailingZerosToPlainString;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import lombok.Data;

@Data
public class SpotDashboardDailyItemVO {

	/** 时间点：yyyy-MM-dd 或 yyyy-MM-dd HH:00 */
	private String date;

	private Long dealCount = 0L;

	@JsonSerialize(using = BigDecimalStripTrailingZerosToPlainString.class)
	private BigDecimal buyDealTotal = BigDecimal.ZERO;

	@JsonSerialize(using = BigDecimalStripTrailingZerosToPlainString.class)
	private BigDecimal sellDealTotal = BigDecimal.ZERO;

	@JsonSerialize(using = BigDecimalStripTrailingZerosToPlainString.class)
	private BigDecimal totalDealTotal = BigDecimal.ZERO;

	@JsonSerialize(using = BigDecimalStripTrailingZerosToPlainString.class)
	private BigDecimal feeUsdtAmount = BigDecimal.ZERO;
}
