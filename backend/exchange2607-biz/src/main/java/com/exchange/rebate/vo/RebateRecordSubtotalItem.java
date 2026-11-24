package com.exchange.rebate.vo;

import java.math.BigDecimal;

import com.exchange.common.json.BigDecimalStripTrailingZerosToPlainString;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import lombok.Data;

/**
 * 按币种分组的返佣小计项
 */
@Data
public class RebateRecordSubtotalItem {

	private String assetCode;

	@JsonSerialize(using = BigDecimalStripTrailingZerosToPlainString.class)
	private BigDecimal totalAmount = BigDecimal.ZERO;

	private Integer count = 0;
}
