package com.exchange.dashboard.vo;

import java.math.BigDecimal;

import com.exchange.common.json.BigDecimalStripTrailingZerosToPlainString;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import lombok.Data;

@Data
public class C2cDashboardDailyItemVO {

	private String date;

	private Long createdCount = 0L;

	private Long completedCount = 0L;

	private Long cancelledCount = 0L;

	@JsonSerialize(using = BigDecimalStripTrailingZerosToPlainString.class)
	private BigDecimal dealAmount = BigDecimal.ZERO;

	@JsonSerialize(using = BigDecimalStripTrailingZerosToPlainString.class)
	private BigDecimal completionRate = BigDecimal.ZERO;

	@JsonSerialize(using = BigDecimalStripTrailingZerosToPlainString.class)
	private BigDecimal cancelRate = BigDecimal.ZERO;

	private Long appealCount = 0L;
}
