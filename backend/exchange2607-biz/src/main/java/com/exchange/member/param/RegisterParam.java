package com.exchange.member.param;

import javax.validation.constraints.NotBlank;

import lombok.Data;

@Data
public class RegisterParam {

	/**
	 * 联系方式类型 email:邮箱，phone:手机（对应 Constant.联系方式_邮箱 / 联系方式_手机号码）
	 */
	@NotBlank
	private String contactType;

	/**
	 * 区域代码，contactType 为手机号时传入
	 */
	private String phoneAreaCode;

	/**
	 * 邮箱号，或不带区域代码的手机号（必须与发码时 contact 保持一致）
	 */
	@NotBlank
	private String contact;

	/**
	 * 验证码（register 业务类型，由 verifyCode 校验）
	 */
	@NotBlank
	private String verificationCode;

	@NotBlank
	private String loginPwd;

	private String inviteCode;

}
