package com.exchange.contract.param;

import java.math.BigDecimal;

import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

import lombok.Data;

@Data
public class PreviewMarginLiqPriceParam {

	/** 由 Controller 注入当前登录会员，防越权 */
	private String memberAccountId;

	@NotBlank
	private String positionId;

	/** 调整后的目标保证金（绝对值，非增量） */
	@NotNull
	@DecimalMin(value = "0.00000001", message = "保证金必须大于0")
	private BigDecimal newMargin;

}
