package com.exchange.common.exception;

import java.util.Locale;

import org.springframework.data.redis.core.StringRedisTemplate;

import com.exchange.common.i18n.I18nUtils;
import com.exchange.common.utils.SpringUtils;
import com.exchange.common.vo.Result;
import com.exchange.constants.Constant;
import com.exchange.multilang.MultiLangHolder;

import cn.dev33.satoken.context.SaHolder;
import cn.dev33.satoken.exception.NotLoginException;
import cn.dev33.satoken.exception.NotRoleException;
import cn.dev33.satoken.stp.StpUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;

/**
 * 鉴权异常处理：运行在 SaServletFilter 中，早于 DispatcherServlet， 此时 LocaleContextHolder
 * 尚未设置，需直接读取 Lang 请求头解析语言。
 */
public class LoginExceptionHelper {

	public static String handle(Throwable e) {
		Locale locale = resolveLocale();
		if (e instanceof NotLoginException) {
			String msgKey = "未登录";
			NotLoginException notLoginException = (NotLoginException) e;
			String type = notLoginException.getType();
			if (NotLoginException.TOKEN_TIMEOUT.equals(type)) {
				msgKey = "登录已过期";
			} else if (NotLoginException.BE_REPLACED.equals(type)) {
				msgKey = "账号在另一设备登录，你已被强制下线";
			} else if (NotLoginException.KICK_OUT.equals(type)) {
				msgKey = resolveKickoutMsgKey(notLoginException);
			}
			return JSONUtil.toJsonStr(Result.fail(BizError.请登录.getCode(), I18nUtils.getMessage(msgKey, locale)));
		} else if (e instanceof NotRoleException) {
			return JSONUtil.toJsonStr(Result.fail(BizError.请登录.getCode(), I18nUtils.getMessage("token权限异常", locale)));
		} else {
			return JSONUtil.toJsonStr(Result.fail(BizError.请登录.getCode(), e.getMessage()));
		}
	}

	/** 被踢下线：若踢出原因是权限变更，给出专属提示；其余沿用默认文案 */
	private static String resolveKickoutMsgKey(NotLoginException e) {
		String defaultMsgKey = "账号在另一设备登录，你已被强制下线";
		try {
			String token = StpUtil.getTokenValue();
			if (StrUtil.isBlank(token)) {
				return defaultMsgKey;
			}
			StringRedisTemplate redis = SpringUtils.getBean(StringRedisTemplate.class);
			String key = Constant.强制下线原因缓存key前缀 + token;
			String reason = redis.opsForValue().get(key);
			if (Constant.强制下线原因_权限变更.equals(reason)) {
				// 不主动删除：前端并发请求可能多次触发，交由 TTL 自动清理
				return "权限已变更，请重新登录";
			}
			if (Constant.强制下线原因_密码变更.equals(reason)) {
				return "密码已修改，请重新登录";
			}
			if (Constant.强制下线原因_账号冻结.equals(reason)) {
				return "账号已被冻结";
			}
			if (Constant.强制下线原因_账号删除.equals(reason)) {
				return "账号已被删除";
			}
			if (Constant.强制下线原因_账号注销.equals(reason)) {
				return "账号已被注销";
			}
		} catch (Exception ex) {
			// Filter 阶段读取失败时回落到默认文案
		}
		return defaultMsgKey;
	}

	/** Filter 阶段 LocaleResolver 尚未执行，直接读取 Lang 请求头 */
	private static Locale resolveLocale() {
		try {
			return MultiLangHolder.resolve(SaHolder.getRequest().getHeader("Lang"));
		} catch (Exception ex) {
			System.out.println("Filter 阶段 Lang报错");
			return MultiLangHolder.defaultLocale();
		}
	}

}
