package com.exchange.verificationcode.domain;

import java.io.Serializable;
import java.time.LocalDateTime;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Version;

import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

import com.exchange.common.utils.IdUtils;
import com.exchange.constants.Constant;

import cn.hutool.core.util.StrUtil;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "verification_code_send_record")
@DynamicInsert(true)
@DynamicUpdate(true)
public class VerificationCodeSendRecord implements Serializable {

	/**
	 *
	 */
	private static final long serialVersionUID = 1L;

	@Id
	@Column(name = "id", length = 32)
	private String id;

	private LocalDateTime createTime;

	private LocalDateTime sendTime;

	private String contactType;

	private String phoneAreaCode;

	private String contact;

	private String bizType;

	private String verificationCode;

	@Column(columnDefinition = "LONGTEXT")
	private String errorMsg;

	private String sendStatus;

	@Column(name = "ip")
	private String ip;

	/**
	 * 发送时确定的语言码（如 zh-CN / zh-TW / en-US）。
	 * <p>
	 * 必须在请求线程内解析并落库：实际发信在 MQ 消费线程执行，届时已无请求上下文，无法再取 Lang 请求头。
	 */
	private String langCode = Constant.默认语言_简体中文;

	@Version
	private Long version;

	public void fail(String errorMsg) {
		this.setSendStatus(Constant.验证码发送状态_发送失败);
		this.setErrorMsg(errorMsg);
		this.setSendTime(LocalDateTime.now());
	}

	public void success() {
		this.setSendStatus(Constant.验证码发送状态_发送成功);
		this.setSendTime(LocalDateTime.now());
	}

	public static VerificationCodeSendRecord build(String contactType, String phoneAreaCode, String contact, String bizType,
			String verificationCode, String ip, String langCode) {
		VerificationCodeSendRecord po = new VerificationCodeSendRecord();
		po.setId(IdUtils.getId());
		po.setCreateTime(LocalDateTime.now());
		po.setContactType(contactType);
		po.setPhoneAreaCode(phoneAreaCode);
		po.setContact(contact);
		po.setBizType(bizType);
		po.setVerificationCode(verificationCode);
		po.setIp(ip);
		po.setLangCode(StrUtil.isBlank(langCode) ? Constant.默认语言_简体中文 : langCode);
		po.setSendStatus(Constant.验证码发送状态_未发送);
		return po;
	}
}
