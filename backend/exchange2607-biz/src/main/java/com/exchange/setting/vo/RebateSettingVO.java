package com.exchange.setting.vo;

import java.math.BigDecimal;

import org.springframework.beans.BeanUtils;

import com.exchange.setting.domain.RebateSetting;

import lombok.Data;

@Data
public class RebateSettingVO {

	private BigDecimal rate;

	public static RebateSettingVO convertFor(RebateSetting setting) {
		RebateSettingVO vo = new RebateSettingVO();
		if (setting != null) {
			BeanUtils.copyProperties(setting, vo);
		}
		return vo;
	}
}
