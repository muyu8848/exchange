package com.exchange.content.vo;

import java.io.Serializable;
import java.util.ArrayList;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.beans.BeanUtils;

import com.exchange.content.domain.Notice;
import com.fasterxml.jackson.annotation.JsonFormat;

import cn.hutool.core.collection.CollectionUtil;
import lombok.Data;

@Data
public class NoticeVO implements Serializable {

	/**
	* 
	*/
	private static final long serialVersionUID = 1L;

	private String id;

	private String title;

	private String content;

	@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
	private LocalDateTime publishTime;

	/** 后台编辑回填：各语言译文（仅 findById 返回，客户端接口不含） */
	private List<NoticeI18nVO> translations;

	public static List<NoticeVO> convertFor(List<Notice> pos) {
		if (CollectionUtil.isEmpty(pos)) {
			return new ArrayList<>();
		}
		List<NoticeVO> vos = new ArrayList<>();
		for (Notice po : pos) {
			vos.add(convertFor(po));
		}
		return vos;
	}

	public static NoticeVO convertFor(Notice po) {
		if (po == null) {
			return null;
		}
		NoticeVO vo = new NoticeVO();
		BeanUtils.copyProperties(po, vo);
		return vo;
	}

}
