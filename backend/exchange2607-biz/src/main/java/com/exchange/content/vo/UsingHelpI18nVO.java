package com.exchange.content.vo;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.BeanUtils;

import com.exchange.content.domain.UsingHelpI18n;

import cn.hutool.core.collection.CollectionUtil;
import lombok.Data;

/**
 * 使用帮助译文（后台编辑回填用，客户端不返回）。
 */
@Data
public class UsingHelpI18nVO {

	private String langCode;

	private String title;

	private String content;

	public static List<UsingHelpI18nVO> convertFor(List<UsingHelpI18n> pos) {
		if (CollectionUtil.isEmpty(pos)) {
			return new ArrayList<>();
		}
		List<UsingHelpI18nVO> vos = new ArrayList<>();
		for (UsingHelpI18n po : pos) {
			vos.add(convertFor(po));
		}
		return vos;
	}

	public static UsingHelpI18nVO convertFor(UsingHelpI18n po) {
		if (po == null) {
			return null;
		}
		UsingHelpI18nVO vo = new UsingHelpI18nVO();
		BeanUtils.copyProperties(po, vo);
		return vo;
	}

}
