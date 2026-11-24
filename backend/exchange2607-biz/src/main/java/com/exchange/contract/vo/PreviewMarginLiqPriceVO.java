package com.exchange.contract.vo;

import java.math.BigDecimal;

import lombok.Data;

@Data
public class PreviewMarginLiqPriceVO {

	/** 调整后参考强平价（已按交易对价格精度舍入） */
	private BigDecimal liquidationPrice;

	/** 调整后保证金率（8 位小数，前端用于判断是否低于安全线） */
	private BigDecimal marginRatio;

}
