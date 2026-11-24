package com.exchange.setting.vo;

import java.math.BigDecimal;

import org.springframework.beans.BeanUtils;

import com.exchange.setting.domain.ConvertSetting;

import lombok.Data;

@Data
public class ConvertSettingVO {

	private Boolean enabled;
	private BigDecimal spread;
	private BigDecimal minAmount;
	private BigDecimal maxAmount;
	private Integer quoteTtlSeconds;

	public static ConvertSettingVO convertFor(ConvertSetting po) {
		ConvertSettingVO vo = new ConvertSettingVO();
		if (po != null) {
			BeanUtils.copyProperties(po, vo);
		}
		return vo;
	}
}
