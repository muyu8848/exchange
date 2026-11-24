package com.exchange.contract.param;

import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

import lombok.Data;

@Data
public class SaveContractLeverageParam {

	@NotBlank
	private String symbolCode;

	/** 开多杠杆倍数 */
	@NotNull
	@DecimalMin(value = "1", inclusive = true)
	private Integer longLeverage;

	/** 开空杠杆倍数 */
	@NotNull
	@DecimalMin(value = "1", inclusive = true)
	private Integer shortLeverage;

	/** 最近一次选择的方向: long / short */
	@NotBlank
	private String direction;

	private String memberAccountId;

}
