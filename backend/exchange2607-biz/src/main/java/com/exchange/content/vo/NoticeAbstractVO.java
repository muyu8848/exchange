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
public class NoticeAbstractVO implements Serializable {

	/**
	* 
	*/
	private static final long serialVersionUID = 1L;

	private String id;

	private String title;

	@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
	private LocalDateTime publishTime;

	public static List<NoticeAbstractVO> convertFor(List<Notice> pos) {
		if (CollectionUtil.isEmpty(pos)) {
			return new ArrayList<>();
		}
		List<NoticeAbstractVO> vos = new ArrayList<>();
		for (Notice po : pos) {
			vos.add(convertFor(po));
		}
		return vos;
	}

	public static NoticeAbstractVO convertFor(Notice po) {
		if (po == null) {
			return null;
		}
		NoticeAbstractVO vo = new NoticeAbstractVO();
		BeanUtils.copyProperties(po, vo);
		return vo;
	}

}
