package com.exchange.contract.vo;

import java.math.BigDecimal;

import lombok.Data;

@Data
public class SecondContractOrderSubtotalVO {

	/** 订单总数 */
	private Integer orderCount = 0;

	/** 进行中单数 */
	private Integer runningCount = 0;

	/** 盈利单数 */
	private Integer winCount = 0;

	/** 亏损单数 */
	private Integer lossCount = 0;

	/** 下注总额 */
	private BigDecimal totalAmount = BigDecimal.ZERO;

	/** 盈亏总额 */
	private BigDecimal totalPnl = BigDecimal.ZERO;
}
