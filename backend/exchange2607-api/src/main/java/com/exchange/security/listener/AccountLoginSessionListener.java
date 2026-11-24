package com.exchange.security.listener;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.exchange.constants.Constant;
import com.exchange.security.service.AccountLoginSessionService;

import cn.dev33.satoken.listener.SaTokenListenerForSimple;
import cn.dev33.satoken.stp.parameter.SaLoginParameter;
import lombok.extern.slf4j.Slf4j;

/**
 * 登录会话监听器：统一维护 account_login_session，异常只记日志，不能影响登录主流程。
 */
@Slf4j
@Component
public class AccountLoginSessionListener extends SaTokenListenerForSimple {

	@Autowired
	private AccountLoginSessionService accountLoginSessionService;

	@Override
	public void doLogin(String loginType, Object loginId, String tokenValue, SaLoginParameter loginParameter) {
		try {
			accountLoginSessionService.createSession(loginId, tokenValue, loginParameter);
		} catch (Exception e) {
			log.error("记录登录会话失败:{}", tokenValue, e);
		}
	}

	@Override
	public void doLogout(String loginType, Object loginId, String tokenValue) {
		safeMarkStatus(tokenValue, Constant.登录会话状态_已登出);
	}

	@Override
	public void doKickout(String loginType, Object loginId, String tokenValue) {
		safeMarkStatus(tokenValue, Constant.登录会话状态_已踢出);
	}

	@Override
	public void doReplaced(String loginType, Object loginId, String tokenValue) {
		safeMarkStatus(tokenValue, Constant.登录会话状态_已顶下线);
	}

	@Override
	public void doRenewTimeout(String loginType, Object loginId, String tokenValue, long timeout) {
		try {
			accountLoginSessionService.touchLastActiveTime(tokenValue);
		} catch (Exception e) {
			log.error("更新会话活跃时间失败:{}", tokenValue, e);
		}
	}

	private void safeMarkStatus(String tokenValue, String status) {
		try {
			accountLoginSessionService.markStatus(tokenValue, status);
		} catch (Exception e) {
			log.error("更新会话状态失败:{},{}", tokenValue, status, e);
		}
	}
}
