package com.exchange.contract.param;

import java.math.BigDecimal;

import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

import lombok.Data;

@Data
public class PerpetualMarketClosePartPositionParam {

	private String memberAccountId;

	@NotBlank
	private String positionId;

	/** 平仓数量 */
	@NotNull
	@DecimalMin(value = "0", inclusive = false)
	private BigDecimal size;

}
