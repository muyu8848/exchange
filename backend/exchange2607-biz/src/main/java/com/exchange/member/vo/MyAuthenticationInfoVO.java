package com.exchange.member.vo;

import java.time.LocalDate;
import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonFormat;

import lombok.Data;

/**
 * 客户端实名认证信息。
 * <p>
 * 与 {@link MemberInfoVO} 分离：账号基本信息接口只保留 authenticationStatus，
 * 证件号码、有效期、驳回原因等仅实名页使用的字段由本 VO 承载。
 */
@Data
public class MyAuthenticationInfoVO {

	/** 认证状态，见 Constant.会员认证状态_* */
	private String status;

	private String statusName;

	/** 证件类型：idCard / passport / driverLicense，见 Constant.证件类型_* */
	private String idType;

	private String idTypeName;

	/**
	 * 脱敏后的证件号码，如 11********34。
	 * <p>
	 * 不下发明文，避免抓包泄露；客户端实名页仅作展示，无需完整号码。
	 */
	private String idNumberMasked;

	private String realName;

	@JsonFormat(pattern = "yyyy-MM-dd")
	private LocalDate expiryDate;

	/** 提交时间 */
	@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
	private LocalDateTime createTime;

	/** 认证通过时间 */
	@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
	private LocalDateTime passedTime;

	/** 最近一次驳回原因 */
	private String failReason;

	/** 最近一次驳回时间 */
	@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
	private LocalDateTime failTime;

}
