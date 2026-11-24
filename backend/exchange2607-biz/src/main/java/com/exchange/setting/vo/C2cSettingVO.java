package com.exchange.setting.vo;

import org.springframework.beans.BeanUtils;

import com.exchange.setting.domain.C2cSetting;

import lombok.Data;

@Data
public class C2cSettingVO {

	private Integer tradeDeadlineMin;

	private Integer tradeDeadlineMax;

	private Integer autoReleaseMinutes;

	private Integer maxActivePreOrders;

	private Integer perOrderMaxPaymentAccounts;

	private Integer protocolCancelHours;

	private Integer appealIntervalMinutes;

	public static C2cSettingVO convertFor(C2cSetting po) {
		C2cSettingVO vo = new C2cSettingVO();
		if (po != null) {
			BeanUtils.copyProperties(po, vo);
		}
		return vo;
	}

}
