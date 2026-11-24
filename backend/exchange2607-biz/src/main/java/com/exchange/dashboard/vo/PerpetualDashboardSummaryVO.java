package com.exchange.dashboard.vo;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import com.exchange.common.json.BigDecimalStripTrailingZerosToPlainString;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import lombok.Data;

@Data
public class PerpetualDashboardSummaryVO {

	private PerpetualDashboardOverviewVO overview = new PerpetualDashboardOverviewVO();

	private List<PerpetualDashboardTrendItemVO> trend = new ArrayList<>();

	/** 交易对 Top 10（按成交额） */
	private List<PerpetualDashboardSymbolItemVO> symbolTop = new ArrayList<>();

	/** 订单类型分布（开仓/平仓/强平） */
	private List<PerpetualDashboardDistributionItemVO> orderTypeDistribution = new ArrayList<>();

	/** 多头持仓保证金（USDT，快照） */
	@JsonSerialize(using = BigDecimalStripTrailingZerosToPlainString.class)
	private BigDecimal longMargin = BigDecimal.ZERO;

	/** 空头持仓保证金（USDT，快照） */
	@JsonSerialize(using = BigDecimalStripTrailingZerosToPlainString.class)
	private BigDecimal shortMargin = BigDecimal.ZERO;
}
