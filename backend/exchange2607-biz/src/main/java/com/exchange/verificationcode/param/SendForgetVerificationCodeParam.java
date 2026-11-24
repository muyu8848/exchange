package com.exchange.verificationcode.param;

import javax.validation.constraints.NotBlank;

import lombok.Data;

/**
 * 发送找回密码验证码入参（未登录场景，与 SendRegisterVerificationCodeParam 对齐）。
 */
@Data
public class SendForgetVerificationCodeParam {

	/**
	 * IP 系统自动传入（Controller 在调用 Service 前注入，Service 层 @Valid 校验）
	 */
	@NotBlank
	private String ip;

	/**
	 * 联系方式类型 email:邮箱，phone:手机（对应 Constant.联系方式_邮箱 / 联系方式_手机号码）
	 */
	@NotBlank
	private String contactType;

	/**
	 * 区域代码，来源为手机号的时候传入
	 */
	private String phoneAreaCode;

	/**
	 * 邮箱号或不带区域代码的手机号（与 ResetPwdParam.contact 对齐）
	 */
	@NotBlank
	private String contact;

}
