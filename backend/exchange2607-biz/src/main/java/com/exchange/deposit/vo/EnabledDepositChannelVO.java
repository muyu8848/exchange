package com.exchange.deposit.vo;

import java.math.BigDecimal;

import org.springframework.beans.BeanUtils;

import com.exchange.deposit.domain.DepositChannel;

import lombok.Data;

@Data
public class EnabledDepositChannelVO {

	private String id;

	private String chainType;

	private String assetCode;

	private BigDecimal minAmount;

	private BigDecimal maxAmount;

	private Boolean available;

	private Integer orderExpireMinutes;

	public static EnabledDepositChannelVO convertFor(DepositChannel po) {
		if (po == null) {
			return null;
		}
		EnabledDepositChannelVO vo = new EnabledDepositChannelVO();
		BeanUtils.copyProperties(po, vo);
		return vo;
	}

}
