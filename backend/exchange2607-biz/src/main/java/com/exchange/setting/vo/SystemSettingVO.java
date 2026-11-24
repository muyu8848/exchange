package com.exchange.setting.vo;

import org.springframework.beans.BeanUtils;

import com.exchange.setting.domain.SystemSetting;

import lombok.Data;

@Data
public class SystemSettingVO {

	private String appDownloadUrl;

	private String appSchema;

	private String h5Url;

	private String customerServiceUrl;

	public static SystemSettingVO convertFor(SystemSetting po) {
		SystemSettingVO vo = new SystemSettingVO();
		if (po != null) {
			BeanUtils.copyProperties(po, vo);
		}
		return vo;
	}

}
