package com.exchange.config;

import java.util.Locale;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.ReloadableResourceBundleMessageSource;
import org.springframework.web.servlet.LocaleResolver;

import com.exchange.constants.Constant;
import com.exchange.multilang.MultiLangHolder;

/**
 * 国际化配置：语言解析器 + 文案资源。
 * <p>
 * 文案位于 classpath:i18n/messages*.properties：
 * messages.properties（简体中文，默认文件兼兜底）、messages_zh_TW.properties、messages_en_US.properties。
 */
@Configuration
public class I18nConfig {

	/** Bean 名必须为 localeResolver，才能覆盖 Spring Boot 默认的 AcceptHeaderLocaleResolver */
	@Bean(name = "localeResolver")
	public LocaleResolver localeResolver() {
		return new ExchangeLocaleResolver();
	}

	/**
	 * 显式定义 MessageSource：避免 MessageSourceAutoConfiguration 的条件装配不确定性（资源缺失时全站 500），
	 * 同时支持翻译校对期热加载。
	 */
	@Bean(name = "messageSource")
	public MessageSource messageSource() {
		ReloadableResourceBundleMessageSource source = new ReloadableResourceBundleMessageSource();
		source.setBasenames("classpath:i18n/messages");
		source.setDefaultEncoding("UTF-8");
		// 服务器 JVM 为英文环境时，也回落到 messages.properties（简体中文）而不是系统语言
		source.setFallbackToSystemLocale(false);
		source.setUseCodeAsDefaultMessage(false);
		source.setCacheSeconds(60);
		return source;
	}

	/**
	 * 客户端语言解析器：只认 Lang 请求头（值为 /client/common/findAllLanguage 下发的语言码），
	 * 未传或无法解析时使用默认语言；运营后台（/admin/**）固定默认语言。
	 */
	static class ExchangeLocaleResolver implements LocaleResolver {

		@Override
		public Locale resolveLocale(HttpServletRequest request) {
			// 运营后台固定默认语言
			if (request.getRequestURI().startsWith(Constant.后台接口前缀)) {
				return MultiLangHolder.defaultLocale();
			}
			return MultiLangHolder.resolve(request.getHeader("Lang"));
		}

		@Override
		public void setLocale(HttpServletRequest request, HttpServletResponse response, Locale locale) {
			// 客户端通过请求头传语言，服务端不写 Cookie
		}
	}

}
