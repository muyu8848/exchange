package com.exchange.member.param;

import javax.validation.constraints.NotBlank;

import com.exchange.common.operlog.SensitiveParam;

import lombok.Data;

/**
 * 后台重置会员资金密码入参。
 * <p>
 * 原接口为 {@code updateFundPwd(String id, String newPwd)} 两个扁平参数，
 * 缺少字段名导致操作日志无法按 key 脱敏，故收敛为对象。
 */
@Data
public class ResetFundPwdParam {

	@NotBlank
	private String id;

	@NotBlank
	@SensitiveParam
	private String newPwd;

}
