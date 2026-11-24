package com.exchange.member.vo;

import java.math.BigDecimal;

import lombok.Data;

@Data
public class AssetByCurrencyVO {

	private String currencyCode;

	/** 可用余额（跨账户汇总） */
	private BigDecimal available;

	/** 冻结余额（跨账户汇总） */
	private BigDecimal frozen;

	/** 总余额 = available + frozen */
	private BigDecimal total;

	/** USDT 估值 */
	private BigDecimal usdtValue;

}
