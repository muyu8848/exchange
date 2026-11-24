package com.exchange.content.vo;

import java.io.Serializable;
import java.util.ArrayList;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.beans.BeanUtils;

import com.exchange.content.domain.UsingHelp;
import com.fasterxml.jackson.annotation.JsonFormat;

import cn.hutool.core.collection.CollectionUtil;
import lombok.Data;

@Data
public class UsingHelpVO implements Serializable {

	/**
	* 
	*/
	private static final long serialVersionUID = 1L;

	private String id;

	private String title;

	private String content;

	@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
	private LocalDateTime latelyUpdateTime;

	/** 后台编辑回填：各语言译文（仅 findById 返回，客户端接口不含） */
	private List<UsingHelpI18nVO> translations;

	public static List<UsingHelpVO> convertFor(List<UsingHelp> pos) {
		if (CollectionUtil.isEmpty(pos)) {
			return new ArrayList<>();
		}
		List<UsingHelpVO> vos = new ArrayList<>();
		for (UsingHelp po : pos) {
			vos.add(convertFor(po));
		}
		return vos;
	}

	public static UsingHelpVO convertFor(UsingHelp po) {
		if (po == null) {
			return null;
		}
		UsingHelpVO vo = new UsingHelpVO();
		BeanUtils.copyProperties(po, vo);
		return vo;
	}

}
