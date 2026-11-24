package com.exchange.member.param;

import javax.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class SendSetFundPwdCodeParam {

	/** IP 系统自动传入 */
	@NotBlank
	private String ip;

	/** 联系方式类型 email:邮箱，phone:手机（对应 Constant.联系方式_邮箱 / 联系方式_手机号码） */
	@NotBlank
	private String contactType;

	/** 会员ID，Controller 层注入 */
	@NotBlank
	private String memberAccountId;
}
