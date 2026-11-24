package com.exchange.dictconfig.vo;

import java.util.ArrayList;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.beans.BeanUtils;

import com.exchange.dictconfig.domain.DictType;
import com.fasterxml.jackson.annotation.JsonFormat;

import cn.hutool.core.collection.CollectionUtil;
import lombok.Data;

@Data
public class DictTypeVO {

	private String id;

	private String dictTypeCode;

	private String dictTypeName;

	private String note;

	@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
	private LocalDateTime lastUpdateTime;

	public static List<DictTypeVO> convertFor(List<DictType> dictTypes) {
		if (CollectionUtil.isEmpty(dictTypes)) {
			return new ArrayList<>();
		}
		List<DictTypeVO> vos = new ArrayList<>();
		for (DictType dictType : dictTypes) {
			vos.add(convertFor(dictType));
		}
		return vos;
	}

	public static DictTypeVO convertFor(DictType dictType) {
		if (dictType == null) {
			return null;
		}
		DictTypeVO vo = new DictTypeVO();
		BeanUtils.copyProperties(dictType, vo);
		return vo;
	}

}
