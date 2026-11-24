package com.exchange.contract.vo;

import java.math.BigDecimal;

import com.exchange.common.json.BigDecimalStripTrailingZerosToPlainString;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import lombok.Data;

/**
 * 永续计划委托小计。
 * <p>
 * 口径：开仓/平仓只统计委托中；待触发保证金为开仓计划委托中的保证金预估（计划委托创建时不冻结保证金）。
 */
@Data
public class PerpetualPlanOrderSubtotalVO {

	/** 委托总数（含全部状态） */
	private Integer orderCount = 0;

	/** 委托中 */
	private Integer pendingCount = 0;

	/** 已委托（触发成功） */
	private Integer triggeredCount = 0;

	/** 已撤销 */
	private Integer canceledCount = 0;

	/** 委托失败 */
	private Integer failedCount = 0;

	/** 已失效 */
	private Integer invalidCount = 0;

	/** 开仓计划（委托中） */
	private Integer pendingOpenCount = 0;

	/** 平仓计划（委托中） */
	private Integer pendingCloseCount = 0;

	/** 待触发保证金（USDT，开仓计划委托中，触发后才会真正占用） */
	@JsonSerialize(using = BigDecimalStripTrailingZerosToPlainString.class)
	private BigDecimal pendingOpenMargin = BigDecimal.ZERO;
}
