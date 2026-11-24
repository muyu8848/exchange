package com.exchange.content.param;

import java.time.LocalDateTime;
import java.util.List;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;

import org.springframework.beans.BeanUtils;

import com.exchange.common.utils.IdUtils;
import com.exchange.content.domain.Notice;

import lombok.Data;

@Data
public class AddOrUpdateNoticeParam {

	private String id;

	/** 标题（默认语言 zh-CN） */
	@NotBlank
	private String title;

	/** 正文（默认语言 zh-CN） */
	@NotBlank
	private String content;

	/** 其他语言译文；标题为空的语言视为未翻译，保存时会被清理 */
	@Valid
	private List<NoticeI18nParam> translations;

	public Notice convertToPo() {
		Notice po = new Notice();
		BeanUtils.copyProperties(this, po);
		po.setId(IdUtils.getId());
		po.setPublishTime(LocalDateTime.now());
		return po;
	}

	/** 单语言译文入参 */
	@Data
	public static class NoticeI18nParam {

		@NotBlank
		private String langCode;

		/** 标题必填（与正文成对提交；不需要翻译该语言时请勿提交该元素） */
		@NotBlank
		private String title;

		/** 正文必填（与标题成对提交） */
		@NotBlank
		private String content;

	}

}
