package com.exchange.multilang.vo;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import lombok.Data;

/**
 * 客户端语言清单：默认语言 + 启用中的语言列表。
 * <p>
 * 默认语言取自 Constant.默认语言_简体中文，供客户端首次进入/未知语言时兜底。
 */
@Data
public class MultiLangListVO implements Serializable {

	private static final long serialVersionUID = 1L;

	/** 默认语言码 */
	private String defaultLang;

	private List<MultiLangOptionVO> languages = new ArrayList<>();

}
