package com.exchange.common.vo;

import lombok.Data;

/**
 * 登录结果值对象。
 * <p>
 * 登录成功时携带 accountId + tokenInfo；登录失败时携带 accountId（账号存在时有值）+ failMsg。
 * Controller 层根据此结果统一记录登录日志，避免在 catch 分支重复查库。
 */
@Data
public class LoginResult {

	private boolean success;

	/**
	 * 登录账号 ID（后台管理员 ID 或客户端会员 ID）。 账号不存在时为 null。
	 */
	private String accountId;

	/**
	 * 失败原因（仅失败时有值）
	 */
	private String failMsg;

	/**
	 * 是否需要谷歌二次验证（账号已绑定谷歌验证器，且本次未提交验证码）
	 */
	private boolean needGoogleAuth;

	/**
	 * 登录令牌信息（仅成功时有值）
	 */
	private TokenInfo tokenInfo;

	public static LoginResult success(String accountId, TokenInfo tokenInfo) {
		LoginResult r = new LoginResult();
		r.success = true;
		r.accountId = accountId;
		r.tokenInfo = tokenInfo;
		return r;
	}

	public static LoginResult fail(String accountId, String failMsg) {
		LoginResult r = new LoginResult();
		r.success = false;
		r.accountId = accountId;
		r.failMsg = failMsg;
		return r;
	}

	/** 密码/验证码已通过，但需要谷歌二次验证 */
	public static LoginResult needGoogleAuth(String accountId) {
		LoginResult r = new LoginResult();
		r.success = false;
		r.accountId = accountId;
		r.needGoogleAuth = true;
		r.failMsg = "请输入谷歌验证码";
		return r;
	}

}
