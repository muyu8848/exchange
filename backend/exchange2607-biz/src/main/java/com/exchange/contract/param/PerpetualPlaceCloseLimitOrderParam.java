package com.exchange.contract.param;

import java.math.BigDecimal;

import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

import lombok.Data;

@Data
public class PerpetualPlaceCloseLimitOrderParam {

	private String memberAccountId;

	@NotBlank
	private String positionId;

	@NotNull
	@DecimalMin(value = "0", inclusive = false)
	private BigDecimal price;

	/** 平仓数量，为空时全平 */
	private BigDecimal size;

}
