package com.exchange.contract.param;

import java.time.LocalDateTime;

import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

import org.springframework.beans.BeanUtils;

import com.exchange.common.utils.IdUtils;
import com.exchange.contract.domain.SecondContractSymbol;

import lombok.Data;

@Data
public class AddOrUpdateSecondContractSymbolParam {

	private String id;

	@NotBlank
	private String symbolCode;

	@NotBlank
	private String baseCurrencyCode;

	@NotBlank
	private String quoteCurrencyCode;

	@NotBlank
	private String status;

	@NotNull
	private Integer priceScale;

	@NotNull
	@DecimalMin(value = "0", inclusive = true)
	private Integer sortNum;

	public SecondContractSymbol convertToPo() {
		SecondContractSymbol po = new SecondContractSymbol();
		BeanUtils.copyProperties(this, po);
		po.setId(IdUtils.getId());
		po.setCreateTime(LocalDateTime.now());
		po.setUpdateTime(po.getCreateTime());
		po.setDeletedFlag(false);
		return po;
	}

}
