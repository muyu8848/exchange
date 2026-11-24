package com.exchange.withdraw.vo;

import java.math.BigDecimal;

import org.springframework.beans.BeanUtils;

import com.exchange.withdraw.domain.WithdrawChannel;

import lombok.Data;

@Data
public class EnabledWithdrawChannelVO {

	private String id;

	private String chainType;

	private String assetCode;

	private BigDecimal minAmount;

	private BigDecimal fixedFee;

	private Integer withdrawScale;

	private String arrivalTimeDesc;

	public static EnabledWithdrawChannelVO convertFor(WithdrawChannel po) {
		if (po == null) {
			return null;
		}
		EnabledWithdrawChannelVO vo = new EnabledWithdrawChannelVO();
		BeanUtils.copyProperties(po, vo);
		return vo;
	}

}
