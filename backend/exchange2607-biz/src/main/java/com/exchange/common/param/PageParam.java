package com.exchange.common.param;

import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.NotNull;

import lombok.Data;

@Data
public class PageParam {

	@NotNull
	@DecimalMin(value = "1", inclusive = true)
	private Integer pageNum = 1;

	@NotNull
	@DecimalMin(value = "1", inclusive = true)
	private Integer pageSize = 10;

	private String orderByPropertie;

	private String orderByDirection;

}
