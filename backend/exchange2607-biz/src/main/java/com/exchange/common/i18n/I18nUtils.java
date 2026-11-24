package com.exchange.common.i18n;

import java.math.BigDecimal;
import java.text.MessageFormat;
import java.util.Locale;

import org.springframework.context.MessageSource;

import com.exchange.common.utils.AmountUtils;
import com.exchange.common.utils.SpringUtils;

import cn.hutool.core.util.StrUtil;

/**
 * 国际化文案工具。
 * <p>
 * 约定：key 即中文原文（带参文案为中文模板，占位符用 {0} {1}）。
 * key 在目标语言中不存在时，用中文模板 + 参数渲染，保证永不返回 null 或 key 名。
 */
public class I18nUtils {

	private I18nUtils() {
	}

	public static String getMessage(String key, Object... args) {
		return getMessage(key, LocaleUtils.currentLocale(), args);
	}

	public static String getMessage(String key, Locale locale, Object... args) {
		if (StrUtil.isBlank(key)) {
			return key;
		}
		Object[] normalized = normalizeArgs(args);
		Locale target = locale == null ? LocaleUtils.defaultLocale() : locale;
		try {
			return SpringUtils.getBean(MessageSource.class).getMessage(key, normalized, key, target);
		} catch (Exception e) {
			// MessageSource 不可用（极早期调用）时直接渲染中文模板
			return render(key, normalized);
		}
	}

	/**
	 * 参数归一化为字符串：
	 * 1. 规避 MessageFormat 对 Number 的千分位与小数截断；
	 * 2. 金额（BigDecimal）去尾零，避免提示出现 1000.00000000。
	 */
	private static Object[] normalizeArgs(Object[] args) {
		if (args == null || args.length == 0) {
			return null;
		}
		Object[] normalized = new Object[args.length];
		for (int i = 0; i < args.length; i++) {
			Object arg = args[i];
			if (arg == null) {
				normalized[i] = StrUtil.EMPTY;
			} else if (arg instanceof BigDecimal) {
				normalized[i] = AmountUtils.toPlainString((BigDecimal) arg);
			} else {
				normalized[i] = StrUtil.toString(arg);
			}
		}
		return normalized;
	}

	private static String render(String pattern, Object[] args) {
		if (args == null || args.length == 0) {
			return pattern;
		}
		return new MessageFormat(pattern, Locale.SIMPLIFIED_CHINESE).format(args);
	}

}
