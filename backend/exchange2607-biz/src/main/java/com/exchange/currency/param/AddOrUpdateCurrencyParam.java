package com.exchange.currency.param;

import java.time.LocalDateTime;

import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

import org.springframework.beans.BeanUtils;

import com.exchange.common.utils.IdUtils;
import com.exchange.currency.domain.Currency;

import lombok.Data;

@Data
public class AddOrUpdateCurrencyParam {

	private String id;

	@NotBlank
	private String currencyCode;

	@NotBlank
	private String currencyName;

	@NotBlank
	private String currencyLogo;

	@NotNull
	private Integer scale;

	@NotNull
	@DecimalMin(value = "0", inclusive = true)
	private Integer sortNum;

	public Currency convertToPo() {
		Currency po = new Currency();
		BeanUtils.copyProperties(this, po);
		po.setId(IdUtils.getId());
		po.setCreateTime(LocalDateTime.now());
		po.setUpdateTime(po.getCreateTime());
		po.setDeletedFlag(false);
		return po;
	}

}
