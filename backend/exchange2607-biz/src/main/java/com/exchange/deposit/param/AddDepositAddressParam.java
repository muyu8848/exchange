package com.exchange.deposit.param;

import java.time.LocalDateTime;

import javax.validation.constraints.NotBlank;

import org.springframework.beans.BeanUtils;

import com.exchange.common.utils.IdUtils;
import com.exchange.deposit.domain.DepositAddress;

import lombok.Data;

@Data
public class AddDepositAddressParam {

	@NotBlank
	private String chainType;

	@NotBlank
	private String address;

	@NotBlank
	private String status;

	public DepositAddress convertToPo() {
		DepositAddress po = new DepositAddress();
		BeanUtils.copyProperties(this, po);
		po.setId(IdUtils.getId());
		po.setAddress(po.getAddress().trim());
		po.setCreateTime(LocalDateTime.now());
		po.setDeletedFlag(false);
		po.setTotalReceived(java.math.BigDecimal.ZERO);
		return po;
	}

}
