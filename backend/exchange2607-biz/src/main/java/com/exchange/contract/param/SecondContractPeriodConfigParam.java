package com.exchange.contract.param;

import java.math.BigDecimal;

import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.NotNull;

import org.springframework.beans.BeanUtils;

import com.exchange.common.utils.IdUtils;
import com.exchange.contract.domain.SecondContractPeriodConfig;

import lombok.Data;

@Data
public class SecondContractPeriodConfigParam {

	@NotNull
	@DecimalMin(value = "0", inclusive = false)
	private Integer period;

	@NotNull
	@DecimalMin(value = "0", inclusive = false)
	private BigDecimal minAmount;

	@NotNull
	@DecimalMin(value = "0", inclusive = false)
	private BigDecimal maxAmount;

	@NotNull
	@DecimalMin(value = "0", inclusive = false)
	private BigDecimal odds;
	
	public SecondContractPeriodConfig convertToPo(String symbolCode) {
		SecondContractPeriodConfig po = new SecondContractPeriodConfig();
		BeanUtils.copyProperties(this, po);
		po.setId(IdUtils.getId());
		po.setSymbolCode(symbolCode);
		return po;
	}

}
