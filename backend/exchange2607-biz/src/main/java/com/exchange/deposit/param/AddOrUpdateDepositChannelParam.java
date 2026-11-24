package com.exchange.deposit.param;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

import org.springframework.beans.BeanUtils;

import com.exchange.common.utils.IdUtils;
import com.exchange.deposit.domain.DepositChannel;

import lombok.Data;

@Data
public class AddOrUpdateDepositChannelParam {

	private String id;

	@NotBlank
	private String chainType;

	@NotBlank
	private String assetCode;

	@NotNull
	private Integer orderExpireMinutes;

	@NotNull
	@DecimalMin(value = "0", inclusive = false)
	private BigDecimal minAmount;

	@NotNull
	@DecimalMin(value = "0", inclusive = false)
	private BigDecimal maxAmount;

	private Integer sortNum;

	@NotBlank
	private String status;

	public DepositChannel convertToPo() {
		DepositChannel po = new DepositChannel();
		BeanUtils.copyProperties(this, po);
		po.setId(IdUtils.getId());
		po.setCreateTime(LocalDateTime.now());
		po.setUpdateTime(po.getCreateTime());
		po.setDeletedFlag(false);
		if (po.getSortNum() == null) {
			po.setSortNum(0);
		}
		return po;
	}

}
