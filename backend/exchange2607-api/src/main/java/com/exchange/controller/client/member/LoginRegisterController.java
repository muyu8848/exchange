package com.exchange.controller.client.member;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import com.exchange.common.exception.BizError;
import com.exchange.common.exception.BizException;
import com.exchange.common.vo.LoginResult;
import com.exchange.common.vo.Result;
import com.exchange.common.vo.TokenInfo;
import com.exchange.constants.Constant;
import com.exchange.log.domain.LoginLog;
import com.exchange.log.service.LoginLogService;
import com.exchange.member.param.LoginParam;
import com.exchange.member.param.RegisterParam;
import com.exchange.member.param.ResetPwdParam;
import com.exchange.member.service.MemberAccountService;
import com.exchange.verificationcode.param.SendForgetVerificationCodeParam;
import com.exchange.verificationcode.param.SendLoginVerificationCodeParam;
import com.exchange.verificationcode.param.SendRegisterVerificationCodeParam;
import com.exchange.verificationcode.service.VerificationCodeService;

import com.exchange.common.utils.IpUtils;

import cn.hutool.http.useragent.UserAgentUtil;

@RestController
public class LoginRegisterController {

	@Autowired
	private MemberAccountService memberAccountService;

	@Autowired
	private LoginLogService loginLogService;

	@Autowired
	private VerificationCodeService verificationCodeService;

	@PostMapping("/register")
	public Result<String> register(RegisterParam param) {
		memberAccountService.register(param);
		return Result.success();
	}

	@PostMapping("/sendRegisterVerificationCode")
	public Result<String> sendRegisterVerificationCode(SendRegisterVerificationCodeParam param,
			HttpServletRequest request) {
		param.setIp(IpUtils.getClientIp(request));
		verificationCodeService.sendRegisterVerificationCode(param);
		return Result.success();
	}

	@PostMapping("/sendForgetVerificationCode")
	public Result<String> sendForgetVerificationCode(SendForgetVerificationCodeParam param,
			HttpServletRequest request) {
		param.setIp(IpUtils.getClientIp(request));
		verificationCodeService.sendForgetVerificationCode(param);
		return Result.success();
	}

	@PostMapping("/sendLoginVerificationCode")
	public Result<String> sendLoginVerificationCode(SendLoginVerificationCodeParam param, HttpServletRequest request) {
		param.setIp(IpUtils.getClientIp(request));
		verificationCodeService.sendLoginVerificationCode(param);
		return Result.success();
	}

	@PostMapping("/resetPwd")
	public Result<String> resetPwd(ResetPwdParam param) {
		memberAccountService.resetPwd(param);
		return Result.success();
	}

	@PostMapping("/login")
	public Result<TokenInfo> login(LoginParam param, HttpServletRequest request) {
		String loginIp = IpUtils.getClientIp(request);
		param.setIp(loginIp);
		LoginLog loginLog = LoginLog.buildLog(null, param.getContact(), Constant.子系统_客户端, loginIp,
				UserAgentUtil.parse(request.getHeader("User-Agent")));
		LoginResult result = memberAccountService.login(param);
		if (result.isSuccess()) {
			loginLogService.recordLoginLog(
					loginLog.loginSuccess(result.getAccountId(), result.getTokenInfo().getTokenValue()));
			return Result.success(result.getTokenInfo());
		}
		if (result.isNeedGoogleAuth()) {
			// 密码已通过但需要谷歌二次验证：不记登录失败日志，返回可识别状态码供前端弹出验证码输入框
			Result<TokenInfo> needGoogleAuth = new Result<>();
			needGoogleAuth.setSuccess(false);
			needGoogleAuth.setCode(BizError.需要谷歌验证.getCode());
			needGoogleAuth.setMsg(result.getFailMsg());
			return needGoogleAuth;
		}
		loginLogService.recordLoginLog(loginLog.loginFail(result.getAccountId(), result.getFailMsg()));
		throw new BizException(result.getFailMsg());
	}

	@PostMapping("/logout")
	public Result<String> logout() {
		memberAccountService.logout();
		return Result.success();
	}

}
