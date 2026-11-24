package com.exchange.content.vo;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.BeanUtils;

import com.exchange.content.domain.UsingHelp;
import com.fasterxml.jackson.annotation.JsonFormat;

import cn.hutool.core.collection.CollectionUtil;
import lombok.Data;

/**
 * 使用帮助列表项（不含正文，减小列表响应体）。
 * <p>
 * 正文请在详情接口 {@link UsingHelpVO} 中获取。
 */
@Data
public class UsingHelpAbstractVO implements Serializable {

	private static final long serialVersionUID = 1L;

	private String id;

	private String title;

	@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
	private LocalDateTime latelyUpdateTime;

	public static List<UsingHelpAbstractVO> convertFor(List<UsingHelp> pos) {
		if (CollectionUtil.isEmpty(pos)) {
			return new ArrayList<>();
		}
		List<UsingHelpAbstractVO> vos = new ArrayList<>();
		for (UsingHelp po : pos) {
			vos.add(convertFor(po));
		}
		return vos;
	}

	public static UsingHelpAbstractVO convertFor(UsingHelp po) {
		if (po == null) {
			return null;
		}
		UsingHelpAbstractVO vo = new UsingHelpAbstractVO();
		BeanUtils.copyProperties(po, vo);
		return vo;
	}

}
