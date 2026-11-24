package com.exchange.config;

import java.io.IOException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import com.exchange.common.vo.Result;
import com.exchange.constants.Constant;

import cn.dev33.satoken.annotation.SaCheckPermission;
import cn.hutool.json.JSONUtil;

/**
 * 演示模式拦截器：演示模式下禁止带 {@link SaCheckPermission} 的按钮级操作接口。
 * <p>
 * 以 {@code @SaCheckPermission} 作为"写操作/敏感操作"的主判断依据（仅后台接口带该注解）：
 * 查询、导出等只读接口不带该注解，天然放行；新增写接口只要按规范加注解即自动拦截。
 * <p>
 * 少数没有 {@code @SaCheckPermission} 但演示模式下仍需禁止的接口（后台本人操作、客户端改密码）， 通过
 * {@link #EXTRA_BLOCK_PATHS} 极简补充。客户端接口一律无该注解，只能靠补充清单命中。
 */
@Component
public class DemoModeInterceptor implements HandlerInterceptor {

	private static final String DEMO_MODE_KEY = "demoMode";

	/**
	 * 补充拦截清单：没有 {@code @SaCheckPermission}，但演示模式下仍需禁止的接口。
	 * <p>
	 * 后台：本人操作（改自己密码、绑自己谷歌）不需要按钮权限。<br>
	 * 客户端：改登录密码会把演示账号改掉，导致后续体验者无法登录。
	 */
	private static final Set<String> EXTRA_BLOCK_PATHS = new HashSet<>(Arrays.asList(
			"/admin/backgroundAccount/bindGoogleAuth", "/admin/backgroundAccount/updateCurrentAccountLoginPwd",
			"/client/memberAccount/updateLoginPwd", "/client/memberAccount/updateFundPwd",
			"/client/memberAccount/changeBindContact", "/client/memberAccount/bindContact"));

	@Autowired
	private StringRedisTemplate redisTemplate;

	@Override
	public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
			throws IOException {
		String uri = request.getRequestURI();
		boolean extraBlock = EXTRA_BLOCK_PATHS.contains(uri);
		// 客户端接口没有 @SaCheckPermission，只有补充清单会命中：非目标路径直接放行，避免整体流量查 Redis
		if (!extraBlock && uri.startsWith(Constant.客户端接口前缀)) {
			return true;
		}
		Boolean demoMode = redisTemplate.hasKey(DEMO_MODE_KEY);
		if (demoMode == null || !demoMode) {
			return true;
		}
		if (extraBlock) {
			return reject(response);
		}
		if (!(handler instanceof HandlerMethod)) {
			return true;
		}
		Method method = ((HandlerMethod) handler).getMethod();
		if (AnnotatedElementUtils.findMergedAnnotation(method, SaCheckPermission.class) == null) {
			return true;
		}
		return reject(response);
	}

	private boolean reject(HttpServletResponse response) throws IOException {
		response.setHeader("Content-Type", "application/json;charset=UTF-8");
		response.getWriter().write(JSONUtil.toJsonStr(Result.fail(403, "演示模式下不允许执行此操作")));
		return false;
	}

}
