package com.exchange.dashboard.vo;

import java.math.BigDecimal;

import com.exchange.common.json.BigDecimalStripTrailingZerosToPlainString;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import lombok.Data;

@Data
public class MemberAssetDashboardTypeItemVO {

	private String accountType;

	private String accountTypeName;

	/** 折合 USDT（跨币种仅展示 USDT 口径） */
	@JsonSerialize(using = BigDecimalStripTrailingZerosToPlainString.class)
	private BigDecimal usdtValue = BigDecimal.ZERO;

	/** 账户记录数 */
	private Long accountCount = 0L;

	/** 占总额比例（%） */
	@JsonSerialize(using = BigDecimalStripTrailingZerosToPlainString.class)
	private BigDecimal ratio = BigDecimal.ZERO;
}
