package com.exchange.dashboard.vo;

import java.math.BigDecimal;

import com.exchange.common.json.BigDecimalStripTrailingZerosToPlainString;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import lombok.Data;

@Data
public class MemberAssetDashboardOverviewVO {

	/** 总资产（USDT） */
	@JsonSerialize(using = BigDecimalStripTrailingZerosToPlainString.class)
	private BigDecimal totalUsdtValue = BigDecimal.ZERO;

	/** 可用资产（USDT） */
	@JsonSerialize(using = BigDecimalStripTrailingZerosToPlainString.class)
	private BigDecimal availableUsdtValue = BigDecimal.ZERO;

	/** 冻结资产（USDT） */
	@JsonSerialize(using = BigDecimalStripTrailingZerosToPlainString.class)
	private BigDecimal frozenUsdtValue = BigDecimal.ZERO;

	/** 有资产会员数（去重） */
	private Long memberCount = 0L;

	/** 资产账户记录数（余额 > 0） */
	private Long accountCount = 0L;

	/** 人均资产（USDT） */
	@JsonSerialize(using = BigDecimalStripTrailingZerosToPlainString.class)
	private BigDecimal avgUsdtValue = BigDecimal.ZERO;

	/** 覆盖币种数 */
	private Long assetCount = 0L;
}
