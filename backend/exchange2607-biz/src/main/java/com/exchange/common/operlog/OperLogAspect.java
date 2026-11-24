package com.exchange.common.operlog;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import com.exchange.common.utils.IdUtils;
import com.exchange.common.utils.IpUtils;
import com.exchange.constants.Constant;
import com.exchange.log.service.OperLogService;

import cn.dev33.satoken.stp.StpUtil;
import cn.hutool.core.util.ArrayUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.extra.servlet.ServletUtil;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Aspect
@Component
public class OperLogAspect {

	@Autowired
	private OperLogService operLogService;

	@Around("@annotation(operLog)")
	public Object around(ProceedingJoinPoint joinPoint, OperLog operLog) throws Throwable {
		long start = System.currentTimeMillis();
		// 方法执行前采集操作人：方法体内可能强制下线（如修改本人密码），执行后就取不到了
		String operAccountId = null;
		String operName = null;
		try {
			if (StpUtil.isLogin()) {
				operAccountId = StpUtil.getLoginIdAsString();
				operName = StpUtil.getSession().getString("accountName");
			}
		} catch (Exception ignored) {
			// 采集失败不阻断业务，操作人留空
		}
		Object result = null;
		Throwable error = null;
		try {
			result = joinPoint.proceed();
			return result;
		} catch (Throwable e) {
			error = e;
			throw e;
		} finally {
			recordOperLog(joinPoint, operLog, error, System.currentTimeMillis() - start, operAccountId, operName);
		}
	}

	private void recordOperLog(JoinPoint joinPoint, OperLog annotation, Throwable error, long costTime,
			String operAccountId, String operName) {
		try {
			if (annotation == null) {
				return;
			}
			ServletRequestAttributes requestAttributes = (ServletRequestAttributes) RequestContextHolder
					.getRequestAttributes();
			if (requestAttributes == null) {
				return;
			}
			HttpServletRequest request = requestAttributes.getRequest();

			com.exchange.log.domain.OperLog operLog = new com.exchange.log.domain.OperLog();
			operLog.setId(IdUtils.getId());
			operLog.setSubSystem(annotation.subSystem());
			operLog.setModule(annotation.module());
			operLog.setOperate(annotation.operate());
			operLog.setRequestMethod(request.getMethod());
			operLog.setRequestUrl(request.getRequestURL().toString());

			operLog.setRequestParam(ServletUtil.isMultipart(request) ? "" : buildRequestParam(joinPoint));
			operLog.setIp(IpUtils.getClientIp(request));
			operLog.setOperAccountId(operAccountId);
			operLog.setOperName(operName);
			operLog.setOperTime(LocalDateTime.now());
			operLog.setStatus(error == null ? Constant.操作日志状态_成功 : Constant.操作日志状态_失败);
			operLog.setCostTime(costTime);
			if (error != null) {
				operLog.setErrorMsg(StrUtil.maxLength(error.getMessage(), 1000));
			}
			operLogService.recordOperLog(operLog);
		} catch (Exception e) {
			log.error("记录操作日志失败", e);
		}
	}

	/**
	 * 构造入库的请求参数。
	 * <p>
	 * 先反射扫描参数对象，收集标注了 {@link SensitiveParam} 的字段名；<br>
	 * 没有标注 → 直接序列化返回（绝大多数接口走这条路径，不做无谓的 JSON 树遍历）；<br>
	 * 有标注 → 解析成 JSON 树逐层替换为 {@code ***}。
	 * <p>
	 * 因此接口参数必须是对象（如 {@code xxxParam}）。
	 * 切勿写成 {@code (String id, String newPwd)} 这类扁平参数——
	 * 那种形态没有字段可标注，也不会被脱敏。
	 */
	private String buildRequestParam(JoinPoint joinPoint) {
		Object[] args = joinPoint.getArgs();
		if (ArrayUtil.isEmpty(args)) {
			return "";
		}

		// 过滤掉 Servlet 容器对象，避免整段 request 被序列化
		List<Object> logArgs = Arrays.stream(args)
				.filter(arg -> (!(arg instanceof HttpServletRequest) && !(arg instanceof HttpServletResponse)))
				.collect(Collectors.toList());

		// 提前判断：收集标注了 @SensitiveParam 的字段名
		Set<String> sensitiveFields = new HashSet<String>();
		for (Object arg : logArgs) {
			collectSensitiveFields(arg, sensitiveFields, 0);
		}

		// 无敏感字段：直接序列化，跳过 JSON 树遍历
		if (sensitiveFields.isEmpty()) {
			return JSONUtil.toJsonStr(logArgs);
		}

		JSONArray jsonArray = JSONUtil.parseArray(logArgs);
		maskSensitive(jsonArray, sensitiveFields);

		return jsonArray.toString();
	}

	/**
	 * 反射遍历对象图，收集标注了 {@link SensitiveParam} 的字段名。
	 * <p>
	 * 递归是为了覆盖「Param 里再套 Param」的场景；depth 上限用于防止异常结构导致的死循环。
	 */
	private void collectSensitiveFields(Object obj, Set<String> out, int depth) {
		if (obj == null || depth > 5) {
			return;
		}
		if (obj instanceof Collection) {
			for (Object item : (Collection<?>) obj) {
				collectSensitiveFields(item, out, depth + 1);
			}
			return;
		}
		if (obj.getClass().isArray()) {
			for (Object item : (Object[]) obj) {
				collectSensitiveFields(item, out, depth + 1);
			}
			return;
		}
		Class<?> clazz = obj.getClass();
		// JDK 类型没有业务字段可读，直接跳过
		if (clazz.getName().startsWith("java.")) {
			return;
		}
		for (Field field : clazz.getDeclaredFields()) {
			if (Modifier.isStatic(field.getModifiers())) {
				continue;
			}
			if (field.isAnnotationPresent(SensitiveParam.class)) {
				out.add(field.getName());
			}
			// 递归嵌套对象。不能按 "java." 前缀提前排除——List / Set 也属于 java.util，
			// 排除后集合里的 Param 永远不会被扫描到（本方法内部已对纯 JDK 类型做了跳过）
			if (!field.getType().isPrimitive()) {
				try {
					field.setAccessible(true);
					collectSensitiveFields(field.get(obj), out, depth + 1);
				} catch (Exception ignored) {
					// 反射失败不影响主流程，该字段退化为明文
				}
			}
		}
	}

	/** 按字段名替换值：同名不同层的字段一并打码 */
	private void maskSensitive(Object node, Set<String> sensitiveFields) {
		if (node instanceof JSONObject) {
			JSONObject obj = (JSONObject) node;
			// 遍历中改值会触发 ConcurrentModificationException，先复制 key 集合
			for (String key : new ArrayList<>(obj.keySet())) {
				if (sensitiveFields.contains(key)) {
					obj.set(key, "***");
				}
				else {
					maskSensitive(obj.get(key), sensitiveFields);
				}
			}
		}
		else if (node instanceof JSONArray) {
			JSONArray arr = (JSONArray) node;
			for (int i = 0; i < arr.size(); i++) {
				maskSensitive(arr.get(i), sensitiveFields);
			}
		}
	}

}
