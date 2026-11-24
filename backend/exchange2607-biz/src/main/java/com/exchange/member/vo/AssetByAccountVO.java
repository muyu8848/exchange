package com.exchange.member.vo;

import java.math.BigDecimal;

import lombok.Data;

@Data
public class AssetByAccountVO {

	private String accountType;

	private String accountTypeName;

	/** 该账户类型下所有资产折算成 USDT 的总量 */
	private BigDecimal usdtValue;

}
