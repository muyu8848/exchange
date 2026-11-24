package com.exchange.verificationcode.param;

import javax.validation.constraints.NotBlank;

import lombok.Data;

@Data
public class VerifyCodeParam {

	/**
	 * 联系方式类型 email:邮箱，phone:手机（对应 Constant.联系方式_邮箱 / 联系方式_手机号码）
	 */
	@NotBlank
	private String contactType;

	/**
	 * 区域代码 来源为手机号的时候传入
	 */
	private String phoneAreaCode;

	/**
	 * 邮箱号或不带区域代码的手机号（必须与发送验证码时传入的 contact 保持一致）
	 */
	@NotBlank
	private String contact;

	/**
	 * 业务类型：register / forgetPwd（对应 Constant.验证码类型_*）
	 */
	@NotBlank
	private String bizType;

	/**
	 * 用户输入的验证码
	 */
	@NotBlank
	private String code;

}
