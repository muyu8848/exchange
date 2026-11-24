package com.exchange.controller.admin.background;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import com.exchange.background.param.LoginParam;
import com.exchange.background.service.BackgroundAccountService;
import com.exchange.common.exception.BizException;
import com.exchange.common.utils.IpUtils;
import com.exchange.common.vo.LoginResult;
import com.exchange.common.vo.Result;
import com.exchange.common.vo.TokenInfo;
import com.exchange.constants.Constant;
import com.exchange.log.domain.LoginLog;
import com.exchange.log.service.LoginLogService;

import cn.hutool.http.useragent.UserAgentUtil;

@Validated
@RestController
public class LoginController {

	@Autowired
	private BackgroundAccountService backgroundAccountService;

	@Autowired
	private LoginLogService loginLogService;

	@PostMapping("/login")
	public Result<TokenInfo> login(LoginParam param, HttpServletRequest request) {
		String loginIp = IpUtils.getClientIp(request);
		param.setIp(loginIp);
		LoginLog loginLog = LoginLog.buildLog(null, param.getUserName(), Constant.子系统_后台管理, loginIp,
				UserAgentUtil.parse(request.getHeader("User-Agent")));

		LoginResult result = backgroundAccountService.login(param);

		if (result.isSuccess()) {
			loginLogService.recordLoginLog(
					loginLog.loginSuccess(result.getAccountId(), result.getTokenInfo().getTokenValue()));
			return Result.success(result.getTokenInfo());
		} else {
			loginLogService.recordLoginLog(loginLog.loginFail(result.getAccountId(), result.getFailMsg()));
			throw new BizException(result.getFailMsg());
		}
	}

	@PostMapping("/logout")
	public Result<String> logout() {
		backgroundAccountService.logout();
		return Result.success();
	}

}
