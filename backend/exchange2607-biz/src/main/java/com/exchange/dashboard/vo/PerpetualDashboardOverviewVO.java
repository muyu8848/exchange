package com.exchange.dashboard.vo;

import java.math.BigDecimal;

import com.exchange.common.json.BigDecimalStripTrailingZerosToPlainString;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import lombok.Data;

@Data
public class PerpetualDashboardOverviewVO {

	/** 成交笔数（已成交订单） */
	private Long orderCount = 0L;

	/** 成交额（USDT，size × price） */
	@JsonSerialize(using = BigDecimalStripTrailingZerosToPlainString.class)
	private BigDecimal dealAmount = BigDecimal.ZERO;

	/** 手续费收入（USDT） */
	@JsonSerialize(using = BigDecimalStripTrailingZerosToPlainString.class)
	private BigDecimal feeUsdtAmount = BigDecimal.ZERO;

	/** 用户已实现盈亏（USDT，正=盈利） */
	@JsonSerialize(using = BigDecimalStripTrailingZerosToPlainString.class)
	private BigDecimal realizedPnl = BigDecimal.ZERO;

	/** 强平笔数 */
	private Long liquidateCount = 0L;

	/** 持仓保证金（USDT，当前快照） */
	@JsonSerialize(using = BigDecimalStripTrailingZerosToPlainString.class)
	private BigDecimal holdingMargin = BigDecimal.ZERO;

	/** 持仓人数（当前快照，去重） */
	private Long holderCount = 0L;

	/** 资金费净额（USDT，用户视角：正=用户收取，负=用户支付） */
	@JsonSerialize(using = BigDecimalStripTrailingZerosToPlainString.class)
	private BigDecimal fundingNetAmount = BigDecimal.ZERO;
}
