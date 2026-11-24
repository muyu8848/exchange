package com.exchange.common.i18n;

import java.util.Locale;

import org.springframework.context.i18n.LocaleContext;
import org.springframework.context.i18n.LocaleContextHolder;

import com.exchange.constants.Constant;

/**
 * 语言上下文工具。
 * <p>
 * 当前请求语言由 exchange2607-api 的 I18nConfig 中 localeResolver 解析后写入 LocaleContextHolder；
 * 定时任务 / MQ 消费等无请求上下文场景固定回落默认语言，不跟随 JVM 默认 locale。
 */
public class LocaleUtils {

	/** 系统默认语言：取自 Constant 常量，不读配置、不查库 */
	private static final String DEFAULT_LANG = Constant.默认语言_简体中文;

	private static final Locale DEFAULT_LOCALE = Locale.forLanguageTag(DEFAULT_LANG);

	private LocaleUtils() {
	}

	/** 当前请求语言：无请求上下文时回落默认语言 */
	public static Locale currentLocale() {
		LocaleContext context = LocaleContextHolder.getLocaleContext();
		if (context != null && context.getLocale() != null) {
			return context.getLocale();
		}
		return DEFAULT_LOCALE;
	}

	/** 默认语言码 */
	public static String defaultLang() {
		return DEFAULT_LANG;
	}

	/** 默认语言 */
	public static Locale defaultLocale() {
		return DEFAULT_LOCALE;
	}

}
