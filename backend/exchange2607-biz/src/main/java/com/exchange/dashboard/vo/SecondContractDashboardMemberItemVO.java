package com.exchange.dashboard.vo;

import java.math.BigDecimal;

import com.exchange.common.json.BigDecimalStripTrailingZerosToPlainString;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import lombok.Data;

/**
 * 秒合约会员盈亏排行项（盈利 Top / 亏损 Top）。
 * <p>
 * 仅统计已结算订单（closeTime 在查询范围内），按会员聚合 pnl。
 */
@Data
public class SecondContractDashboardMemberItemVO {

	private String memberAccountId;

	/** 会员 UID */
	private Long memberUid;

	/** 已结算笔数 */
	private Long orderCount = 0L;

	/** 下注总额（USDT） */
	@JsonSerialize(using = BigDecimalStripTrailingZerosToPlainString.class)
	private BigDecimal totalAmount = BigDecimal.ZERO;

	/** 盈亏净额（USDT，正=盈利） */
	@JsonSerialize(using = BigDecimalStripTrailingZerosToPlainString.class)
	private BigDecimal totalPnl = BigDecimal.ZERO;
}
