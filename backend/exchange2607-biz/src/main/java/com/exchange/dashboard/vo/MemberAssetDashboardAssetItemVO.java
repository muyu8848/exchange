package com.exchange.dashboard.vo;

import java.math.BigDecimal;

import com.exchange.common.json.BigDecimalStripTrailingZerosToPlainString;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import lombok.Data;

@Data
public class MemberAssetDashboardAssetItemVO {

	private String assetCode;

	/** 可用（原币种） */
	@JsonSerialize(using = BigDecimalStripTrailingZerosToPlainString.class)
	private BigDecimal available = BigDecimal.ZERO;

	/** 冻结（原币种） */
	@JsonSerialize(using = BigDecimalStripTrailingZerosToPlainString.class)
	private BigDecimal frozen = BigDecimal.ZERO;

	/** 合计（原币种） */
	@JsonSerialize(using = BigDecimalStripTrailingZerosToPlainString.class)
	private BigDecimal total = BigDecimal.ZERO;

	/** 折合 USDT */
	@JsonSerialize(using = BigDecimalStripTrailingZerosToPlainString.class)
	private BigDecimal usdtValue = BigDecimal.ZERO;

	/** 账户记录数 */
	private Long accountCount = 0L;

	/** 占总额比例（%） */
	@JsonSerialize(using = BigDecimalStripTrailingZerosToPlainString.class)
	private BigDecimal ratio = BigDecimal.ZERO;
}
