package com.exchange.dashboard.vo;

import java.math.BigDecimal;

import com.exchange.common.json.BigDecimalStripTrailingZerosToPlainString;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import lombok.Data;

@Data
public class ConvertDashboardDailyItemVO {

	/** 时间点：yyyy-MM-dd 或 yyyy-MM-dd HH:00 */
	private String date;

	private Long convertCount = 0L;

	@JsonSerialize(using = BigDecimalStripTrailingZerosToPlainString.class)
	private BigDecimal convertUsdtValue = BigDecimal.ZERO;

	@JsonSerialize(using = BigDecimalStripTrailingZerosToPlainString.class)
	private BigDecimal feeUsdtAmount = BigDecimal.ZERO;

	/** 当日兑换用户数（当日去重） */
	private Long memberCount = 0L;
}
