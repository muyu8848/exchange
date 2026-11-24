package com.exchange.setting.vo;

import org.springframework.beans.BeanUtils;

import com.exchange.setting.domain.C2cRiskSetting;

import lombok.Data;

@Data
public class C2cRiskSettingVO {

	private Long buyerCancelTrade;

	private Long buyerUnPaid;

	private Long notMySelfPaid;

	private Long timeoutUnPaid;

	private Long buyerProtocolCancel;

	private Long sellerProtocolCancel;

	private Long tier1Duration;

	private Long tier2Duration;

	private Long tier3Duration;

	private Long tier4Duration;

	public static C2cRiskSettingVO convertFor(C2cRiskSetting po) {
		C2cRiskSettingVO vo = new C2cRiskSettingVO();
		if (po != null) {
			BeanUtils.copyProperties(po, vo);
		}
		return vo;
	}

}
