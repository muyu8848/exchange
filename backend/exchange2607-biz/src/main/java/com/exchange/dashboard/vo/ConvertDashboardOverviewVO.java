package com.exchange.dashboard.vo;

import java.math.BigDecimal;

import com.exchange.common.json.BigDecimalStripTrailingZerosToPlainString;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import lombok.Data;

@Data
public class ConvertDashboardOverviewVO {

	/** 兑换笔数 */
	private Long convertCount = 0L;

	/** 兑换金额（USDT，用户支付侧价值） */
	@JsonSerialize(using = BigDecimalStripTrailingZerosToPlainString.class)
	private BigDecimal convertUsdtValue = BigDecimal.ZERO;

	/** 手续费收入（USDT，即点差收益，不重复计） */
	@JsonSerialize(using = BigDecimalStripTrailingZerosToPlainString.class)
	private BigDecimal feeUsdtAmount = BigDecimal.ZERO;

	/** 兑换用户数（去重） */
	private Long memberCount = 0L;

	/** 平均单笔（USDT） */
	@JsonSerialize(using = BigDecimalStripTrailingZerosToPlainString.class)
	private BigDecimal avgUsdtValue = BigDecimal.ZERO;

	/** 实际点差率（%，手续费 / 兑换金额 × 100） */
	@JsonSerialize(using = BigDecimalStripTrailingZerosToPlainString.class)
	private BigDecimal spreadRate = BigDecimal.ZERO;
}
