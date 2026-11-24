package com.exchange.notification.domain;

import java.io.Serializable;
import java.time.LocalDateTime;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;
import org.hibernate.annotations.NotFound;
import org.hibernate.annotations.NotFoundAction;

import com.exchange.member.domain.MemberAccount;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "member_notification")
@DynamicInsert(true)
@DynamicUpdate(true)
public class MemberNotification implements Serializable {

	private static final long serialVersionUID = 1L;

	@Id
	@Column(name = "id", length = 32)
	private String id;

	@Column(name = "member_account_id", length = 32)
	private String memberAccountId;

	@NotFound(action = NotFoundAction.IGNORE)
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "member_account_id", updatable = false, insertable = false)
	private MemberAccount memberAccount;

	private String type;

	private String title;

	private String content;

	@Column(name = "biz_id")
	private String bizId;

	@Column(name = "template_code")
	private String templateCode;

	@Column(name = "template_params")
	private String templateParams;

	@Column(name = "read_flag")
	private Boolean readFlag;

	@Column(name = "read_time")
	private LocalDateTime readTime;

	@Column(name = "popup_flag")
	private Boolean popupFlag;

	@Column(name = "create_time")
	private LocalDateTime createTime;

}
