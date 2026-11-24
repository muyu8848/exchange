package com.exchange.setting.vo;

import org.springframework.beans.BeanUtils;

import com.exchange.setting.domain.OssSetting;

import lombok.Data;

@Data
public class OssSettingVO {

	private String type;

	private String localStoragePath;

	private String publicFetchUrl;

	public static OssSettingVO convertFor(OssSetting po) {
		OssSettingVO vo = new OssSettingVO();
		if (po != null) {
			BeanUtils.copyProperties(po, vo);
		}
		return vo;
	}

}
