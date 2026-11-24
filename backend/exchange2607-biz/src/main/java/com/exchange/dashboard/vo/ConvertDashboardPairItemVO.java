package com.exchange.dashboard.vo;

import java.math.BigDecimal;

import com.exchange.common.json.BigDecimalStripTrailingZerosToPlainString;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import lombok.Data;

@Data
public class ConvertDashboardPairItemVO {

	private String fromAssetCode;

	private String toAssetCode;

	/** 展示用：FROM → TO */
	private String pair;

	private Long convertCount = 0L;

	@JsonSerialize(using = BigDecimalStripTrailingZerosToPlainString.class)
	private BigDecimal convertUsdtValue = BigDecimal.ZERO;
}
