package com.exchange.content.vo;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.BeanUtils;

import com.exchange.content.domain.NoticeI18n;

import cn.hutool.core.collection.CollectionUtil;
import lombok.Data;

/**
 * 公告译文（后台编辑回填用，客户端不返回）。
 */
@Data
public class NoticeI18nVO {

	private String langCode;

	private String title;

	private String content;

	public static List<NoticeI18nVO> convertFor(List<NoticeI18n> pos) {
		if (CollectionUtil.isEmpty(pos)) {
			return new ArrayList<>();
		}
		List<NoticeI18nVO> vos = new ArrayList<>();
		for (NoticeI18n po : pos) {
			vos.add(convertFor(po));
		}
		return vos;
	}

	public static NoticeI18nVO convertFor(NoticeI18n po) {
		if (po == null) {
			return null;
		}
		NoticeI18nVO vo = new NoticeI18nVO();
		BeanUtils.copyProperties(po, vo);
		return vo;
	}

}
