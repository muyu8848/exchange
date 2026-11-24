package com.exchange.dashboard.vo;

import java.math.BigDecimal;

import com.exchange.common.json.BigDecimalStripTrailingZerosToPlainString;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import lombok.Data;

/**
 * 秒合约趋势/按日明细项。
 * <p>
 * 同一天内，orderCount/totalAmount（按 openTime）与
 * completedCount/totalPnl/winCount/lossCount（按 closeTime）可能来自不同批订单。
 */
@Data
public class SecondContractDashboardTrendItemVO {

	private String date;

	/** 下单笔数（按 openTime） */
	private Long orderCount = 0L;

	/** 已结算笔数（按 closeTime） */
	private Long completedCount = 0L;

	/** 下注总额（按 openTime） */
	@JsonSerialize(using = BigDecimalStripTrailingZerosToPlainString.class)
	private BigDecimal totalAmount = BigDecimal.ZERO;

	/** 用户盈亏净额（按 closeTime） */
	@JsonSerialize(using = BigDecimalStripTrailingZerosToPlainString.class)
	private BigDecimal totalPnl = BigDecimal.ZERO;

	/** 盈利单数（按 closeTime） */
	private Long winCount = 0L;

	/** 亏损单数（按 closeTime） */
	private Long lossCount = 0L;
}
