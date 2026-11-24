package com.exchange.dashboard.vo;

import java.math.BigDecimal;

import com.exchange.common.json.BigDecimalStripTrailingZerosToPlainString;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import lombok.Data;

@Data
public class SpotDashboardOverviewVO {

	/** 成交笔数 */
	private Long dealCount = 0L;

	/** 买入成交额（USDT，双边口径） */
	@JsonSerialize(using = BigDecimalStripTrailingZerosToPlainString.class)
	private BigDecimal buyDealTotal = BigDecimal.ZERO;

	/** 卖出成交额（USDT，双边口径） */
	@JsonSerialize(using = BigDecimalStripTrailingZerosToPlainString.class)
	private BigDecimal sellDealTotal = BigDecimal.ZERO;

	/** 总成交额（USDT，买入 + 卖出） */
	@JsonSerialize(using = BigDecimalStripTrailingZerosToPlainString.class)
	private BigDecimal totalDealTotal = BigDecimal.ZERO;

	/** 手续费收入（USDT） */
	@JsonSerialize(using = BigDecimalStripTrailingZerosToPlainString.class)
	private BigDecimal feeUsdtAmount = BigDecimal.ZERO;

	/** 计划委托中笔数（当前快照） */
	private Long pendingPlanCount = 0L;

	/** 待触发金额（USDT，triggerPrice × amount 估算，当前快照） */
	@JsonSerialize(using = BigDecimalStripTrailingZerosToPlainString.class)
	private BigDecimal pendingPlanAmount = BigDecimal.ZERO;
}
