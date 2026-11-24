package com.exchange.content.param;

import java.time.LocalDateTime;
import java.util.List;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;

import org.springframework.beans.BeanUtils;

import com.exchange.common.utils.IdUtils;
import com.exchange.content.domain.UsingHelp;

import lombok.Data;

@Data
public class AddOrUpdateUsingHelpParam {

	private String id;

	/** 标题（默认语言 zh-CN） */
	@NotBlank
	private String title;

	/** 正文（默认语言 zh-CN） */
	@NotBlank
	private String content;

	/** 其他语言译文；标题为空的语言视为未翻译，保存时会被清理 */
	@Valid
	private List<UsingHelpI18nParam> translations;

	public UsingHelp convertToPo() {
		UsingHelp po = new UsingHelp();
		BeanUtils.copyProperties(this, po);
		po.setId(IdUtils.getId());
		po.setLatelyUpdateTime(LocalDateTime.now());
		return po;
	}

	/** 单语言译文入参 */
	@Data
	public static class UsingHelpI18nParam {

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
