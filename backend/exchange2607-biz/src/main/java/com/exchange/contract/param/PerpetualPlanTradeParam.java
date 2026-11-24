package com.exchange.contract.param;

import java.math.BigDecimal;

import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

import lombok.Data;

@Data
public class PerpetualPlanTradeParam {

	/** open=开仓计划, close=平仓计划 */
	@NotBlank
	private String planType;

	/** 开仓计划：交易对 */
	private String symbolCode;

	/** 开仓计划：方向 long / short */
	private String direction;

	/** 开仓计划：杠杆 */
	private Integer leverage;

	/** 开仓计划：保证金 */
	@DecimalMin(value = "0", inclusive = false)
	private BigDecimal margin;

	/** 平仓计划：持仓ID */
	private String positionId;

	/** 触发价 */
	@NotNull
	@DecimalMin(value = "0", inclusive = false)
	private BigDecimal triggerPrice;

	/** 执行方式：limit=限价, market=市价 */
	@NotBlank
	private String execType;

	/** 委托价（限价执行时必填） */
	private BigDecimal price;

	private String memberAccountId;

}
