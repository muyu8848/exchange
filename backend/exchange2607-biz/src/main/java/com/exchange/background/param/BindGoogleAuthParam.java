package com.exchange.background.param;

import javax.validation.constraints.NotBlank;

import com.exchange.common.operlog.SensitiveParam;

import lombok.Data;

/**
 * 绑定谷歌验证器入参。
 * <p>
 * 原接口为两个扁平参数（密钥、验证码），缺少字段名导致操作日志无法按 key 脱敏，
 * 故收敛为对象。
 */
@Data
public class BindGoogleAuthParam {

	@NotBlank
	@SensitiveParam
	private String googleSecretKey;

	@NotBlank
	@SensitiveParam
	private String googleVerCode;

}
