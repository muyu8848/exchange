package com.exchange.contract.param;

import java.math.BigDecimal;

import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

import lombok.Data;

@Data
public class AdjustPositionMarginParam {

	/** 由 Controller 注入当前登录会员，防越权 */
	private String memberAccountId;

	@NotBlank
	private String positionId;

	/** ADD 增加 / REDUCE 减少 */
	@NotBlank
	private String type;

	/** 调整数量（恒为正数） */
	@NotNull
	@DecimalMin(value = "0.00000001", message = "调整数量必须大于0")
	private BigDecimal amount;

}
