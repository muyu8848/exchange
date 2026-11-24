package com.exchange.multilang;

import java.util.Locale;

import com.exchange.common.i18n.LocaleUtils;

import cn.hutool.core.util.StrUtil;

/**
 * 语言解析门面：供 LocaleResolver 与无请求上下文场景静态调用。
 * <p>
 * 语言清单由客户端调用 /client/common/findAllLanguage 获取，服务端不做白名单校验：
 * 传入未支持的语言码时，文案资源自动回落 messages.properties（简体中文）。
 */
public class MultiLangHolder {

	/** 默认语言：Constant.默认语言_简体中文（zh-CN） */
	public static Locale defaultLocale() {
		return LocaleUtils.defaultLocale();
	}

	/**
	 * 把请求头 Lang 解析为 Locale。
	 * <p>
	 * 不做语言白名单校验：格式合法但未知的语言码会得到对应 Locale，
	 * 查不到 messages_xx.properties 时由 ResourceBundle 回落默认文件。
	 */
	public static Locale resolve(String tag) {
		if (StrUtil.isBlank(tag)) {
			return defaultLocale();
		}
		try {
			Locale locale = Locale.forLanguageTag(tag.trim());
			// 非法或空标签会得到 ROOT，显式回落默认语言
			return StrUtil.isEmpty(locale.getLanguage()) ? defaultLocale() : locale;
		} catch (Exception e) {
			// forLanguageTag 对格式非法的标签抛 IllformedLocaleException，不能冒到请求层
			return defaultLocale();
		}
	}

}
