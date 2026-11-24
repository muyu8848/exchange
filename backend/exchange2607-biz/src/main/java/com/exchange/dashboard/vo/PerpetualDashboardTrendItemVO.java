package com.exchange.dashboard.vo;

import java.math.BigDecimal;

import com.exchange.common.json.BigDecimalStripTrailingZerosToPlainString;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import lombok.Data;

@Data
public class PerpetualDashboardTrendItemVO {

	private String date;

	@JsonSerialize(using = BigDecimalStripTrailingZerosToPlainString.class)
	private BigDecimal dealAmount = BigDecimal.ZERO;

	@JsonSerialize(using = BigDecimalStripTrailingZerosToPlainString.class)
	private BigDecimal feeUsdtAmount = BigDecimal.ZERO;

	private Long liquidateCount = 0L;
}
