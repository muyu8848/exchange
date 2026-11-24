package com.exchange.common.exception;

import java.util.Iterator;

import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import javax.validation.Path.Node;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.exchange.common.i18n.I18nUtils;
import com.exchange.common.vo.Result;
import com.exchange.constants.Constant;
import com.zengtengpeng.excepiton.LockException;

import cn.dev33.satoken.exception.NotLoginException;
import cn.dev33.satoken.exception.NotPermissionException;
import cn.dev33.satoken.stp.StpUtil;
import cn.hutool.core.util.StrUtil;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestControllerAdvice
public class RestExceptionHandler {

	@Autowired
	private StringRedisTemplate redisTemplate;

	@ExceptionHandler(NotLoginException.class)
	@ResponseStatus(value = HttpStatus.OK)
	public Result<String> handleNotLoginException(NotLoginException e) {
		// 文案 key 即中文原文，由 I18nUtils 按当前请求语言翻译
		String msgKey = "未登录";
		if (e != null) {
			String type = e.getType();
			if (NotLoginException.TOKEN_TIMEOUT.equals(type)) {
				msgKey = "登录已过期";
			} else if (NotLoginException.BE_REPLACED.equals(type)) {
				msgKey = "账号在另一设备登录，你已被强制下线";
			} else if (NotLoginException.KICK_OUT.equals(type)) {
				msgKey = resolveKickoutMsgKey(e);
			}
			log.warn(e.getMessage());
		}
		return Result.fail(BizError.请登录.getCode(), I18nUtils.getMessage(msgKey));
	}

	/** 被踢下线：若踢出原因是权限变更，给出专属提示；其余沿用默认文案 */
	private String resolveKickoutMsgKey(NotLoginException e) {
		String defaultMsgKey = "账号在另一设备登录，你已被强制下线";
		try {
			String token = StpUtil.getTokenValue();
			if (StrUtil.isBlank(token)) {
				return defaultMsgKey;
			}
			String key = Constant.强制下线原因缓存key前缀 + token;
			String reason = redisTemplate.opsForValue().get(key);
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
			log.warn("读取强制下线原因失败", ex);
		}
		return defaultMsgKey;
	}

	/**
	 * 按钮级权限不足：@SaCheckPermission 校验失败时抛出。
	 * 前端会据此提示并刷新页面，避免用户反复点击无效按钮。
	 */
	@ExceptionHandler(NotPermissionException.class)
	@ResponseStatus(value = HttpStatus.OK)
	public Result<String> handleNotPermissionException(NotPermissionException e) {
		log.warn("无权限操作:{}", e.getMessage());
		return Result.fail(I18nUtils.getMessage("无权限执行该操作"));
	}

	@ExceptionHandler(ObjectOptimisticLockingFailureException.class)
	@ResponseStatus(value = HttpStatus.OK)
	public Result<String> handleOptimisticLockException(ObjectOptimisticLockingFailureException e) {
		// 乐观锁冲突：同一数据被并发修改，提示重试
		log.warn("乐观锁冲突:{}", e.getMessage());
		return Result.fail(I18nUtils.getMessage("操作过于频繁，请重试"));
	}

	@ExceptionHandler(LockException.class)
	@ResponseStatus(value = HttpStatus.OK)
	public Result<String> handleLockException(LockException e) {
		String msg = "lock exception";
		if (e != null) {
			msg = e.getMessage();
			log.warn(e.toString());
		}
		return Result.fail(msg);
	}

	@ExceptionHandler(BizException.class)
	@ResponseStatus(value = HttpStatus.OK)
	public Result<String> handleBizException(BizException e) {
		String msg = "biz exception";
		if (e != null) {
			// key 即中文原文（或中文模板），按当前请求语言翻译，未命中回落中文
			msg = I18nUtils.getMessage(e.getMsg(), e.getArgs());
			log.warn(e.toString());
		}
		return Result.fail(BizError.业务异常.getCode(), msg);
	}

	@ExceptionHandler(ConstraintViolationException.class)
	@ResponseStatus(value = HttpStatus.OK)
	public Result<String> handleConstraintViolationException(ConstraintViolationException e) {
		String msg = "param valid exception";
		if (e != null) {
			Iterator<ConstraintViolation<?>> iterator = e.getConstraintViolations().iterator();
			if (iterator.hasNext()) {
				ConstraintViolation<?> violation = iterator.next();
				String paramName = "";
				for (Node node : violation.getPropertyPath()) {
					paramName = node.getName();
				}
				msg = paramName + " " + violation.getMessage();
			}
			log.warn(e.toString());
		}
		return Result.fail(BizError.参数异常.getCode(), msg);
	}

	@ExceptionHandler(value = BindException.class)
	@ResponseStatus(value = HttpStatus.OK)
	public Result<String> handleBindException(BindException e) {
		String msg = "param valid exception";
		if (e != null) {
			Iterator<FieldError> iterator = e.getBindingResult().getFieldErrors().iterator();
			if (iterator.hasNext()) {
				FieldError fieldError = iterator.next();
				String paramName = fieldError.getField();
				msg = paramName + " " + fieldError.getDefaultMessage();
			}
		}
		return Result.fail(BizError.参数异常.getCode(), msg);
	}

}
