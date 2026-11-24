package com.exchange.verificationcode.param;

import javax.validation.constraints.NotBlank;

import lombok.Data;

@Data
public class SendLoginVerificationCodeParam {

	/**
	 * IP 系统自动传入
	 */
	private String ip;

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
	 * 邮箱号或不带区域代码的手机号（与 LoginParam.contact 对齐）
	 */
	@NotBlank
	private String contact;

}
