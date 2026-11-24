package com.exchange.content.vo;

import java.io.Serializable;
import java.time.LocalDateTime;

import org.springframework.beans.BeanUtils;

import com.exchange.content.domain.UsingHelp;
import com.fasterxml.jackson.annotation.JsonFormat;

import lombok.Data;

/**
 * 客户端使用帮助详情（只暴露展示字段）。
 * <p>
 * 运营后台请使用 {@link UsingHelpVO}，避免管理字段泄漏到客户端。
 */
@Data
public class UsingHelpDetailVO implements Serializable {

	private static final long serialVersionUID = 1L;

	private String id;

	private String title;

	private String content;

	@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
	private LocalDateTime latelyUpdateTime;

	public static UsingHelpDetailVO convertFor(UsingHelp po) {
		if (po == null) {
			return null;
		}
		UsingHelpDetailVO vo = new UsingHelpDetailVO();
		BeanUtils.copyProperties(po, vo);
		return vo;
	}

}
