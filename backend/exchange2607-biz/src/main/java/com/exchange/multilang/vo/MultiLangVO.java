package com.exchange.multilang.vo;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.BeanUtils;

import com.exchange.dictconfig.DictHolder;
import com.exchange.multilang.domain.MultiLang;

import cn.hutool.core.collection.CollectionUtil;
import lombok.Data;

/**
 * 运营后台多语言列表 VO（含状态名称）。
 * <p>
 * 客户端语言清单使用 {@link MultiLangOptionVO}，不含状态等管理字段。
 */
@Data
public class MultiLangVO {

	private String id;

	/** 语言编码：zh-CN / zh-TW / en-US */
	private String langCode;

	/** 语言名称（中文标注，后台展示） */
	private String langName;

	/** 母语名称（客户端下拉框展示） */
	private String nativeName;

	private String status;

	private String statusName;

	private Integer sortNum;

	private LocalDateTime createTime;

	private LocalDateTime updateTime;

	public static List<MultiLangVO> convertFor(List<MultiLang> pos) {
		if (CollectionUtil.isEmpty(pos)) {
			return new ArrayList<>();
		}
		List<MultiLangVO> vos = new ArrayList<>();
		for (MultiLang po : pos) {
			vos.add(convertFor(po));
		}
		return vos;
	}

	public static MultiLangVO convertFor(MultiLang po) {
		if (po == null) {
			return null;
		}
		MultiLangVO vo = new MultiLangVO();
		BeanUtils.copyProperties(po, vo);
		vo.setStatusName(DictHolder.getDictItemName("enableStatus", vo.getStatus()));
		return vo;
	}

}
