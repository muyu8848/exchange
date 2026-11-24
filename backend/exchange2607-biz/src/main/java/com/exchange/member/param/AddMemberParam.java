package com.exchange.member.param;

import javax.validation.constraints.NotBlank;

import com.exchange.common.operlog.SensitiveParam;

import lombok.Data;

@Data
public class AddMemberParam {

	/** 联系方式类型 email:邮箱，phone:手机（对应 Constant.联系方式_邮箱 / 联系方式_手机号码） */
	@NotBlank
	private String contactType;

	/** 区域代码，contactType 为手机号时传入 */
	private String phoneAreaCode;

	/** 邮箱号，或不带区域代码的手机号 */
	@NotBlank
	private String contact;

	@NotBlank
	@SensitiveParam
	private String loginPwd;

	/** 邀请码（选填，用于挂上级） */
	private String inviteCode;

}
