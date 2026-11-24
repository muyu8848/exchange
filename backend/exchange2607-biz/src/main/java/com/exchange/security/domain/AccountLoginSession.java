package com.exchange.security.domain;

import java.io.Serializable;
import java.time.LocalDateTime;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

import lombok.Getter;
import lombok.Setter;

/**
 * 账号登录会话：会员与后台账号共用，按 accountType 区分。
 */
@Getter
@Setter
@Entity
@Table(name = "account_login_session")
@DynamicInsert(true)
@DynamicUpdate(true)
public class AccountLoginSession implements Serializable {

	private static final long serialVersionUID = 1L;

	@Id
	@Column(name = "id", length = 32)
	private String id;

	private String accountId;

	/** MEMBER / ADMIN */
	private String accountType;

	/** 会员 UID，后台账号为空 */
	private Long accountUid;

	/** 会员登录账号（手机号/邮箱）或后台账号 userName */
	private String accountName;

	@Column(length = 128)
	private String token;

	private String deviceType;

	private String ip;

	private LocalDateTime loginTime;

	private LocalDateTime lastActiveTime;

	/** ONLINE / LOGGED_OUT / KICKED / REPLACED / EXPIRED */
	private String status;

	/** 会话结束时间：登出/踢出/顶下线/过期 */
	private LocalDateTime logoutTime;

	private LocalDateTime createTime;
}
