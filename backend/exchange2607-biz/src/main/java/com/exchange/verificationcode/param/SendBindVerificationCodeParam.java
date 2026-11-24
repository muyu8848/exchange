package com.exchange.verificationcode.param;

import javax.validation.constraints.NotBlank;

import lombok.Data;

/**
 * 绑定联系方式发码入参（已登录场景）。
 */
@Data
public class SendBindVerificationCodeParam {

	/**
	 * 会员账号 ID：由 Controller 覆盖为当前登录会员，客户端传入无效
	 */
	@NotBlank
	private String memberAccountId;

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
	 * 区域代码 来源为手机号的时候传入
	 */
	private String phoneAreaCode;

	/**
	 * 邮箱号或不带区域代码的手机号
	 */
	@NotBlank
	private String contact;

}
