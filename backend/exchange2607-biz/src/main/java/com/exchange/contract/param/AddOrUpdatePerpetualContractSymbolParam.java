package com.exchange.contract.param;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import javax.validation.constraints.DecimalMax;
import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

import org.springframework.beans.BeanUtils;

import com.exchange.common.utils.IdUtils;
import com.exchange.contract.domain.PerpetualContractSymbol;

import lombok.Data;

@Data
public class AddOrUpdatePerpetualContractSymbolParam {

	private String id;

	@NotBlank
	private String symbolCode;

	@NotBlank
	private String baseCurrencyCode;

	@NotBlank
	private String quoteCurrencyCode;

	@NotNull
	@DecimalMin(value = "1", inclusive = true)
	private Integer maxLeverage;

	@NotNull
	@DecimalMin(value = "1", inclusive = true)
	private Integer defaultLeverage;

	@NotNull
	@DecimalMin(value = "0", inclusive = false)
	private BigDecimal maintenanceMarginRate;

	@NotNull
	@DecimalMin(value = "0", inclusive = false)
	private BigDecimal takerFeeRate;

	@NotNull
	@DecimalMin(value = "0", inclusive = false)
	@DecimalMax(value = "24")
	private Integer fundingInterval;

	@NotNull
	@DecimalMin(value = "0", inclusive = false)
	private BigDecimal defaultFundingRate;

	@NotNull
	@DecimalMin(value = "0", inclusive = true)
	private Integer priceScale;

	@NotNull
	@DecimalMin(value = "0", inclusive = true)
	private Integer amountScale;

	@NotNull
	@DecimalMin(value = "0", inclusive = false)
	private BigDecimal minOrderValue;

	@NotBlank
	private String status;

	@NotNull
	@DecimalMin(value = "0", inclusive = false)
	private Integer sortNum;

	public PerpetualContractSymbol convertToPo() {
		if (defaultLeverage != null && maxLeverage != null && defaultLeverage > maxLeverage) {
			throw new com.exchange.common.exception.BizException("默认杠杆不能超过最大杠杆");
		}
		PerpetualContractSymbol po = new PerpetualContractSymbol();
		BeanUtils.copyProperties(this, po);
		po.setId(IdUtils.getId());
		po.setCreateTime(LocalDateTime.now());
		po.setUpdateTime(po.getCreateTime());
		po.setDeletedFlag(false);
		return po;
	}

}
