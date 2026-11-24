package com.exchange.member.vo;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.BeanUtils;

import com.exchange.member.domain.CountryAreaCode;

import cn.hutool.core.collection.CollectionUtil;
import lombok.Data;

/**
 * 客户端国家区号选项（下拉用，不含状态等管理字段）。
 */
@Data
public class CountryAreaCodeOptionVO {

	private String areaCode;

	private String countryName;

	private String countryNameEn;

	private String flagImage;

	public static List<CountryAreaCodeOptionVO> convertFor(List<CountryAreaCode> pos) {
		if (CollectionUtil.isEmpty(pos)) {
			return new ArrayList<>();
		}
		List<CountryAreaCodeOptionVO> vos = new ArrayList<>();
		for (CountryAreaCode po : pos) {
			vos.add(convertFor(po));
		}
		return vos;
	}

	public static CountryAreaCodeOptionVO convertFor(CountryAreaCode po) {
		if (po == null) {
			return null;
		}
		CountryAreaCodeOptionVO vo = new CountryAreaCodeOptionVO();
		BeanUtils.copyProperties(po, vo);
		return vo;
	}

}
