package com.exchange.log.domain;

import java.io.Serializable;
import java.time.LocalDateTime;

import javax.persistence.Column;
import javax.persistence.ConstraintMode;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.ForeignKey;
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

/**
 * 会员联系方式绑定流水（注册 / 首次绑定 / 换绑均追加记录，用于追溯某号码历史绑定过的账号）
 */
@Getter
@Setter
@Entity
@Table(name = "member_contact_bind_log")
@DynamicInsert(true)
@DynamicUpdate(true)
public class MemberContactBindLog implements Serializable {

	private static final long serialVersionUID = 1L;

	@Id
	@Column(name = "id", length = 32)
	private String id;

	/** 联系方式类型 email:邮箱 phone:手机（Constant.联系方式_*） */
	private String contactType;

	/** 手机号或邮箱（明文存储） */
	private String contact;

	/** 手机区号，邮箱时为空 */
	private String phoneAreaCode;

	/** 动作：register / bind / unbind */
	private String action;

	private LocalDateTime createTime;

	@Column(name = "member_account_id", nullable = false)
	private String memberAccountId;

	@NotFound(action = NotFoundAction.IGNORE)
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "member_account_id", updatable = false, insertable = false, foreignKey = @ForeignKey(value = ConstraintMode.NO_CONSTRAINT))
	private MemberAccount memberAccount;

}
