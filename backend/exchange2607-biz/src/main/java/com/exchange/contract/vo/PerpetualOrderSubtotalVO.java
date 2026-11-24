package com.exchange.contract.vo;

import java.math.BigDecimal;

import com.exchange.common.json.BigDecimalStripTrailingZerosToPlainString;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import lombok.Data;

/**
 * 永续合约订单小计。
 * <p>
 * 口径：订单总数为全部状态；成交/开平强/手续费/盈亏均只统计已成交(status=1)。
 */
@Data
public class PerpetualOrderSubtotalVO {

	/** 订单总数（含待成交/已成交/已撤销） */
	private Integer orderCount = 0;

	/** 已成交笔数 */
	private Integer filledCount = 0;

	/** 已撤销笔数 */
	private Integer canceledCount = 0;

	/** 开仓笔数（已成交） */
	private Integer openCount = 0;

	/** 平仓笔数（已成交） */
	private Integer closeCount = 0;

	/** 强平笔数（已成交） */
	private Integer liquidateCount = 0;

	/** 手续费合计（USDT，已成交） */
	@JsonSerialize(using = BigDecimalStripTrailingZerosToPlainString.class)
	private BigDecimal totalFee = BigDecimal.ZERO;

	/** 已实现盈亏合计（USDT，已成交） */
	@JsonSerialize(using = BigDecimalStripTrailingZerosToPlainString.class)
	private BigDecimal totalPnl = BigDecimal.ZERO;
}
