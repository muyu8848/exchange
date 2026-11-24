package com.exchange.contract.param;

import java.util.List;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;

import lombok.Data;

@Data
public class UpdateSecondContractPeriodConfigParam {
	

	@NotBlank
	private String symbolCode;

	@Valid
	private List<SecondContractPeriodConfigParam> configs;

}
