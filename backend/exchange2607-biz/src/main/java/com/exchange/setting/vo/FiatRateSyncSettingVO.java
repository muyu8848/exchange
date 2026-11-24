package com.exchange.setting.vo;

import org.springframework.beans.BeanUtils;

import com.exchange.setting.domain.FiatRateSyncSetting;

import lombok.Data;

@Data
public class FiatRateSyncSettingVO {

	private String exchangeRateSource;

	private Boolean autoSyncExchangeRate;

	public static FiatRateSyncSettingVO convertFor(FiatRateSyncSetting po) {
		FiatRateSyncSettingVO vo = new FiatRateSyncSettingVO();
		if (po != null) {
			BeanUtils.copyProperties(po, vo);
		}
		return vo;
	}

}
