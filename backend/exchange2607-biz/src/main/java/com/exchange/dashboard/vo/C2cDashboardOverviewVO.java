package com.exchange.dashboard.vo;

import java.math.BigDecimal;

import com.exchange.common.json.BigDecimalStripTrailingZerosToPlainString;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import lombok.Data;

@Data
public class C2cDashboardOverviewVO {

	/** 创建订单数（按创建时间） */
	private Long createdCount = 0L;

	/** 成交订单数（按完成时间） */
	private Long completedCount = 0L;

	/** 成交额（USDT，按完成时间） */
	@JsonSerialize(using = BigDecimalStripTrailingZerosToPlainString.class)
	private BigDecimal dealAmount = BigDecimal.ZERO;

	/** 完成率（%） */
	@JsonSerialize(using = BigDecimalStripTrailingZerosToPlainString.class)
	private BigDecimal completionRate = BigDecimal.ZERO;

	/** 取消率（%） */
	@JsonSerialize(using = BigDecimalStripTrailingZerosToPlainString.class)
	private BigDecimal cancelRate = BigDecimal.ZERO;

	/** 申诉中订单（当前快照） */
	private Long appealingCount = 0L;

	/** 平均放币时长（秒，按完成时间） */
	private Long avgReleaseSeconds = 0L;
}
