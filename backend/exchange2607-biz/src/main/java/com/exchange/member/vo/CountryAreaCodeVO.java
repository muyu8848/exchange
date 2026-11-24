package com.exchange.member.vo;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.BeanUtils;

import com.exchange.dictconfig.DictHolder;
import com.exchange.member.domain.CountryAreaCode;

import cn.hutool.core.collection.CollectionUtil;
import lombok.Data;

/**
 * 运营后台国家区号列表 VO（含状态等信息）。
 * <p>
 * 客户端下拉使用 {@link CountryAreaCodeOptionVO}，不暴露状态等管理字段。
 */
@Data
public class CountryAreaCodeVO {

	private String id;

	private String areaCode;

	private String countryName;

	private String countryNameEn;

	private String flagImage;

	private String status;

	private String statusName;

	private Integer sortNum;

	private LocalDateTime createTime;

	private LocalDateTime updateTime;

	public static List<CountryAreaCodeVO> convertFor(List<CountryAreaCode> pos) {
		if (CollectionUtil.isEmpty(pos)) {
			return new ArrayList<>();
		}
		List<CountryAreaCodeVO> vos = new ArrayList<>();
		for (CountryAreaCode po : pos) {
			vos.add(convertFor(po));
		}
		return vos;
	}

	public static CountryAreaCodeVO convertFor(CountryAreaCode po) {
		if (po == null) {
			return null;
		}
		CountryAreaCodeVO vo = new CountryAreaCodeVO();
		BeanUtils.copyProperties(po, vo);
		vo.setStatusName(DictHolder.getDictItemName("enableStatus", vo.getStatus()));
		return vo;
	}

}
