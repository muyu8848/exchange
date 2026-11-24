package com.exchange.withdraw.vo;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import com.exchange.common.json.BigDecimalStripTrailingZerosToPlainString;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import lombok.Data;

@Data
public class WithdrawSubtotalVO {

	private Integer orderCount = 0;

	private Integer successCount = 0;

	@JsonSerialize(using = BigDecimalStripTrailingZerosToPlainString.class)
	private BigDecimal totalAmount = BigDecimal.ZERO;

	@JsonSerialize(using = BigDecimalStripTrailingZerosToPlainString.class)
	private BigDecimal successAmount = BigDecimal.ZERO;

	@JsonSerialize(using = BigDecimalStripTrailingZerosToPlainString.class)
	private BigDecimal paidUsdtAmount = BigDecimal.ZERO;

	/** 手续费折合USDT（已打款单） */
	@JsonSerialize(using = BigDecimalStripTrailingZerosToPlainString.class)
	private BigDecimal feeUsdtAmount = BigDecimal.ZERO;

	private List<AssetSubtotalItem> items = new ArrayList<>();

	@Data
	public static class AssetSubtotalItem {
		private String assetCode;
		
		private Integer orderCount = 0;
		
		private Integer successCount = 0;
		
		@JsonSerialize(using = BigDecimalStripTrailingZerosToPlainString.class)
		private BigDecimal totalAmount = BigDecimal.ZERO;
		
		@JsonSerialize(using = BigDecimalStripTrailingZerosToPlainString.class)
		private BigDecimal successAmount = BigDecimal.ZERO;
	}

}
