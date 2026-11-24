package com.exchange.dashboard.vo;

import java.math.BigDecimal;

import com.exchange.common.json.BigDecimalStripTrailingZerosToPlainString;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import lombok.Data;

@Data
public class InvestmentDashboardOverviewVO {

	/** 申购金额（USDT，按申购时间） */
	@JsonSerialize(using = BigDecimalStripTrailingZerosToPlainString.class)
	private BigDecimal subscribeAmount = BigDecimal.ZERO;

	/** 赎回金额（USDT，按赎回时间，含收益） */
	@JsonSerialize(using = BigDecimalStripTrailingZerosToPlainString.class)
	private BigDecimal redeemAmount = BigDecimal.ZERO;

	/** 净流入（USDT）= 申购 − 赎回 */
	@JsonSerialize(using = BigDecimalStripTrailingZerosToPlainString.class)
	private BigDecimal netInflow = BigDecimal.ZERO;

	/** 利息发放（USDT，按利息日期，日汇总表） */
	@JsonSerialize(using = BigDecimalStripTrailingZerosToPlainString.class)
	private BigDecimal interestAmount = BigDecimal.ZERO;

	/** 应计未发放利息（USDT，定期持仓中按天累计的已确认收益，当前快照） */
	@JsonSerialize(using = BigDecimalStripTrailingZerosToPlainString.class)
	private BigDecimal accruedUnpaidInterestAmount = BigDecimal.ZERO;

	/** 当前持仓规模（USDT，快照：定期本金 + 活期持仓） */
	@JsonSerialize(using = BigDecimalStripTrailingZerosToPlainString.class)
	private BigDecimal holdingAmount = BigDecimal.ZERO;

	/** 当前持仓人数（快照，定期持仓中 + 活期有持仓，去重） */
	private Long holderCount = 0L;

	/** 待折算USDT笔数（快照，四类流水合计） */
	private Long pendingConvertCount = 0L;
}
