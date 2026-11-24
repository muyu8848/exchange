package com.exchange.multilang.vo;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import com.exchange.multilang.domain.MultiLang;

import cn.hutool.core.collection.CollectionUtil;
import lombok.Data;

/**
 * 客户端语言选项：仅暴露展示所需字段（不含启用标记等管理字段）。
 */
@Data
public class MultiLangOptionVO implements Serializable {

	private static final long serialVersionUID = 1L;

	/** 语言码：zh-CN / zh-TW / en-US */
	private String langCode;

	/** 语言名称（中文标注） */
	private String langName;

	/** 母语名称（不翻译，如「繁體中文」） */
	private String nativeName;

	private Integer sortNum;

	public static List<MultiLangOptionVO> convertFor(List<MultiLang> pos) {
		if (CollectionUtil.isEmpty(pos)) {
			return new ArrayList<>();
		}
		List<MultiLangOptionVO> vos = new ArrayList<>();
		for (MultiLang po : pos) {
			vos.add(convertFor(po));
		}
		return vos;
	}

	public static MultiLangOptionVO convertFor(MultiLang po) {
		if (po == null) {
			return null;
		}
		MultiLangOptionVO vo = new MultiLangOptionVO();
		vo.setLangCode(po.getLangCode());
		vo.setLangName(po.getLangName());
		vo.setNativeName(po.getNativeName());
		vo.setSortNum(po.getSortNum());
		return vo;
	}

}
