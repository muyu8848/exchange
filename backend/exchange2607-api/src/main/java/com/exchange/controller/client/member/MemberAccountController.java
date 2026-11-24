package com.exchange.controller.client.member;

import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.exchange.common.googleauth.GoogleAuthenticator;
import com.exchange.common.vo.PageResult;
import com.exchange.common.vo.Result;
import com.exchange.constants.Constant;
import com.exchange.log.param.LoginLogQueryCondParam;
import com.exchange.log.service.LoginLogService;
import com.exchange.log.vo.MyLoginLogVO;
import com.exchange.member.param.BindContactParam;
import com.exchange.member.param.ChangeBindContactParam;
import com.exchange.member.param.SendSetFundPwdCodeParam;
import com.exchange.member.param.SetFundPwdParam;
import com.exchange.member.param.SubmitAuthenticationInfoParam;
import com.exchange.member.param.UpdateLoginPwdParam;
import com.exchange.member.param.UpdateFundPwdParam;
import com.exchange.security.service.MemberAccountLoginSessionService;
import com.exchange.member.service.MemberAccountService;
import com.exchange.member.service.MemberAuthenticationInfoService;
import com.exchange.security.vo.MemberAccountLoginSessionVO;
import com.exchange.member.vo.MemberInfoVO;
import com.exchange.member.vo.MyAuthenticationInfoVO;
import com.exchange.verificationcode.param.SendBindVerificationCodeParam;
import com.exchange.verificationcode.param.SendRebindVerificationCodeParam;

import com.exchange.common.utils.IpUtils;

import cn.dev33.satoken.stp.StpUtil;

@RestController
@RequestMapping("/memberAccount")
public class MemberAccountController {

	@Autowired
	private MemberAccountService memberAccountService;

	@Autowired
	private MemberAuthenticationInfoService memberAuthenticationInfoService;

	@Autowired
	private LoginLogService loginLogService;

	@Autowired
	private MemberAccountLoginSessionService memberAccountLoginSessionService;

	@PostMapping("/findMyLoginSession")
	public Result<List<MemberAccountLoginSessionVO>> findMyLoginSession() {
		return Result.success(
				memberAccountLoginSessionService.findByMemberId(StpUtil.getLoginIdAsString(), StpUtil.getTokenValue()));
	}

	@PostMapping("/kickoutLoginSession")
	public Result<String> kickoutLoginSession(String id) {
		memberAccountLoginSessionService.kickout(StpUtil.getLoginIdAsString(), id);
		return Result.success();
	}

	@PostMapping("/kickoutOtherLoginSession")
	public Result<String> kickoutOtherLoginSession() {
		memberAccountLoginSessionService.kickoutOthers(StpUtil.getLoginIdAsString(), StpUtil.getTokenValue());
		return Result.success();
	}

	@PostMapping("/updateNotifyLangCode")
	public Result<String> updateNotifyLangCode(String notifyLangCode) {
		memberAccountService.updateNotifyLangCode(StpUtil.getLoginIdAsString(), notifyLangCode);
		return Result.success();
	}

	@PostMapping("/sendBindVerificationCode")
	public Result<String> sendBindVerificationCode(SendBindVerificationCodeParam param, HttpServletRequest request) {
		param.setIp(IpUtils.getClientIp(request));
		param.setMemberAccountId(StpUtil.getLoginIdAsString());
		memberAccountService.sendBindVerificationCode(param);
		return Result.success();
	}

	@PostMapping("/bindContact")
	public Result<String> bindContact(BindContactParam param) {
		param.setMemberAccountId(StpUtil.getLoginIdAsString());
		memberAccountService.bindContact(param);
		return Result.success();
	}

	@PostMapping("/sendRebindVerificationCode")
	public Result<String> sendRebindVerificationCode(SendRebindVerificationCodeParam param,
			HttpServletRequest request) {
		param.setIp(IpUtils.getClientIp(request));
		param.setMemberAccountId(StpUtil.getLoginIdAsString());
		memberAccountService.sendRebindVerificationCode(param);
		return Result.success();
	}

	@PostMapping("/changeBindContact")
	public Result<String> changeBindContact(ChangeBindContactParam param) {
		param.setMemberAccountId(StpUtil.getLoginIdAsString());
		memberAccountService.changeBindContact(param);
		return Result.success();
	}

	/**
	 * 获取实名认证信息（状态、证件类型、脱敏号码、驳回原因等），仅供实名页使用
	 */
	@PostMapping("/getMyAuthenticationInfo")
	public Result<MyAuthenticationInfoVO> getMyAuthenticationInfo() {
		return Result.success(memberAuthenticationInfoService.getMyAuthenticationInfo(StpUtil.getLoginIdAsString()));
	}

	@PostMapping("/submitAuthenticationInfo")
	@ResponseBody
	public Result<String> submitAuthenticationInfo(SubmitAuthenticationInfoParam param) {
		param.setMemberAccountId(StpUtil.getLoginIdAsString());
		memberAuthenticationInfoService.submitAuthenticationInfo(param);
		return Result.success();
	}

	@PostMapping("/generateGoogleSecretKey")
	@ResponseBody
	public Result<String> generateGoogleSecretKey() {
		return Result.success(GoogleAuthenticator.generateSecretKey());
	}

	@PostMapping("/bindGoogleAuth")
	public Result<String> bindGoogleAuth(String googleSecretKey, String googleVerCode) {
		memberAccountService.bindGoogleAuth(StpUtil.getLoginIdAsString(), googleSecretKey, googleVerCode);
		return Result.success();
	}

	@PostMapping("/getMemberInfo")
	public Result<MemberInfoVO> getMemberInfo() {
		return Result.success(memberAccountService.getMemberInfo(StpUtil.getLoginIdAsString()));
	}

	@PostMapping("/updateNickName")
	public Result<String> updateNickName(String nickName) {
		memberAccountService.updateNickName(StpUtil.getLoginIdAsString(), nickName);
		return Result.success();
	}

	@PostMapping("/updateLoginPwd")
	public Result<String> updateLoginPwd(UpdateLoginPwdParam param) {
		String memberId = StpUtil.getLoginIdAsString();
		param.setMemberAccountId(memberId);
		memberAccountService.updateLoginPwd(param);
		// 密码变更后强制退出所有会话（含当前），要求使用新密码重新登录
		memberAccountLoginSessionService.kickoutByMemberId(memberId, Constant.强制下线原因_密码变更);
		return Result.success();
	}

	@PostMapping("/updateFundPwd")
	public Result<String> updateFundPwd(UpdateFundPwdParam param) {
		param.setMemberAccountId(StpUtil.getLoginIdAsString());
		memberAccountService.updateFundPwd(param);
		return Result.success();
	}

	@PostMapping("/sendSetFundPwdCode")
	@ResponseBody
	public Result<String> sendSetFundPwdCode(SendSetFundPwdCodeParam param, HttpServletRequest request) {
		param.setMemberAccountId(StpUtil.getLoginIdAsString());
		param.setIp(IpUtils.getClientIp(request));
		memberAccountService.sendSetFundPwdCode(param);
		return Result.success();
	}

	@PostMapping("/setFundPwd")
	@ResponseBody
	public Result<String> setFundPwd(SetFundPwdParam param) {
		param.setMemberAccountId(StpUtil.getLoginIdAsString());
		memberAccountService.setFundPwd(param);
		return Result.success();
	}

	@PostMapping("/findLoginLogByPage")
	public Result<PageResult<MyLoginLogVO>> findMyLoginLogByPage(LoginLogQueryCondParam param) {
		param.setAccountId(StpUtil.getLoginIdAsString());
		param.setSubSystem(Constant.子系统_客户端);
		return Result.success(loginLogService.findMyLoginLogByPage(param));
	}

}
