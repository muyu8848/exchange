package com.exchange.dashboard.vo;

import java.math.BigDecimal;

import com.exchange.common.json.BigDecimalStripTrailingZerosToPlainString;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import lombok.Data;

/**
 * 秒合约看板概览。
 * <p>
 * 下单类指标（orderCount/totalAmount/holderCount）按 openTime 统计；
 * 结算类指标（completedCount/totalPnl/winCount/lossCount/drawCount/winRate）按 closeTime 统计；
 * 敞口类指标（running*）为当前快照，不受时间筛选影响。
 */
@Data
public class SecondContractDashboardOverviewVO {

	/** 下单笔数（按 openTime，含进行中与已完结） */
	private Long orderCount = 0L;

	/** 下注总额（USDT，按 openTime） */
	@JsonSerialize(using = BigDecimalStripTrailingZerosToPlainString.class)
	private BigDecimal totalAmount = BigDecimal.ZERO;

	/** 已结算笔数（按 closeTime） */
	private Long completedCount = 0L;

	/** 用户盈亏净额（USDT，正=用户盈利=平台亏损） */
	@JsonSerialize(using = BigDecimalStripTrailingZerosToPlainString.class)
	private BigDecimal totalPnl = BigDecimal.ZERO;

	/** 平台净收益（USDT，= -用户盈亏，正=平台盈利） */
	@JsonSerialize(using = BigDecimalStripTrailingZerosToPlainString.class)
	private BigDecimal platformNetIncome = BigDecimal.ZERO;

	/** 盈利单数 */
	private Long winCount = 0L;

	/** 亏损单数 */
	private Long lossCount = 0L;

	/** 打平单数（开平价格相同，pnl = 0） */
	private Long drawCount = 0L;

	/** 用户胜率（%，= 盈利单 ÷ (盈利单 + 亏损单)，打平不计入分母） */
	@JsonSerialize(using = BigDecimalStripTrailingZerosToPlainString.class)
	private BigDecimal winRate = BigDecimal.ZERO;

	/** 参与人数（按 openTime 去重会员数） */
	private Long holderCount = 0L;

	/** 平均下注额（USDT） */
	@JsonSerialize(using = BigDecimalStripTrailingZerosToPlainString.class)
	private BigDecimal avgAmount = BigDecimal.ZERO;

	/** 进行中笔数（当前快照） */
	private Long runningCount = 0L;

	/** 未结算本金（USDT，进行中订单下注额合计，当前快照） */
	@JsonSerialize(using = BigDecimalStripTrailingZerosToPlainString.class)
	private BigDecimal runningAmount = BigDecimal.ZERO;

	/** 最大潜在赔付（USDT，进行中订单 Σ(下注额 × 订单赔率)，当前快照） */
	@JsonSerialize(using = BigDecimalStripTrailingZerosToPlainString.class)
	private BigDecimal runningMaxPayout = BigDecimal.ZERO;
}
