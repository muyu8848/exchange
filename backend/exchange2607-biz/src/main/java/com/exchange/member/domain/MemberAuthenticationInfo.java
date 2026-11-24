package com.exchange.member.domain;

import java.io.Serializable;
import java.time.LocalDate;
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

import com.exchange.common.utils.IdUtils;
import com.exchange.constants.Constant;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "member_authentication_info")
@DynamicInsert(true)
@DynamicUpdate(true)
public class MemberAuthenticationInfo implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	@Id
	@Column(name = "id", length = 32)
	private String id;

	private String status;

	/** 驳回原因（用户端可见），引导用户修正后重新提交 */
	@Column(name = "fail_reason", length = 500)
	private String failReason;

	private String realName;

	/**
	 * 证件类型：idCard 身份证 / passport 护照 / driverLicense 驾驶证，见 Constant.证件类型_*
	 */
	@Column(name = "id_type", length = 32)
	private String idType;

	private String idNumber;

	/** 证件正面 / 护照资料页 */
	private String identityCardPic1;

	/** 证件反面（护照场景为空） */
	private String identityCardPic2;

	/** 证件有效期：护照、驾驶证必填，身份证选填 */
	@Column(name = "expiry_date")
	private LocalDate expiryDate;

	/** 签发国家/地区二字码（本期只留字段，不采集） */
	@Column(name = "issuing_country", length = 8)
	private String issuingCountry;

	private LocalDateTime createTime;

	private LocalDateTime dealTime;

	@Column(name = "member_account_id", length = 32)
	private String memberAccountId;

	@NotFound(action = NotFoundAction.IGNORE)
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "member_account_id", updatable = false, insertable = false, foreignKey = @ForeignKey(value = ConstraintMode.NO_CONSTRAINT))
	private MemberAccount memberAccount;

	public void authenticationPassed() {
		this.setStatus(Constant.会员认证状态_已认证);
		this.setDealTime(LocalDateTime.now());
	}

	public void authenticationFail(String failReason) {
		this.setStatus(Constant.会员认证状态_审核不通过);
		this.setDealTime(LocalDateTime.now());
		this.setFailReason(failReason);
	}

	public static MemberAuthenticationInfo build(String memberAccountId, String realName, String identityCardPic1,
			String identityCardPic2) {
		MemberAuthenticationInfo po = new MemberAuthenticationInfo();
		po.setId(IdUtils.getId());
		po.setMemberAccountId(memberAccountId);
		po.setRealName(realName);
		po.setIdentityCardPic1(identityCardPic1);
		po.setIdentityCardPic2(identityCardPic2);
		po.setCreateTime(LocalDateTime.now());
		return po;
	}

}
