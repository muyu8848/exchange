package com.exchange.contract.param;

import java.math.BigDecimal;

import javax.validation.constraints.NotBlank;

import lombok.Data;

/**
 * 设置持仓止盈止损入参。
 * takeProfitPrice / stopLossPrice 至少传一个；传具体数值=设置/更新，传空=删除该边。
 */
@Data
public class PerpetualSetTpslParam {

	/** 持仓ID */
	@NotBlank
	private String positionId;

	/** 止盈价（可选，空=不设置/删除） */
	private BigDecimal takeProfitPrice;

	/** 止损价（可选，空=不设置/删除） */
	private BigDecimal stopLossPrice;

	private String memberAccountId;

}
