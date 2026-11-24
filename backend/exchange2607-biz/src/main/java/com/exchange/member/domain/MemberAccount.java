package com.exchange.member.domain;

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
import javax.persistence.Version;

import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;
import org.hibernate.annotations.NotFound;
import org.hibernate.annotations.NotFoundAction;

import com.exchange.common.exception.BizException;
import com.exchange.common.utils.IdUtils;
import com.exchange.common.utils.PwdUtils;
import com.exchange.common.utils.RedisUtils;
import com.exchange.constants.Constant;
import com.exchange.multilang.domain.MultiLang;

import cn.hutool.core.util.StrUtil;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "member_account")
@DynamicInsert(true)
@DynamicUpdate(true)
public class MemberAccount implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	@Id
	@Column(name = "id", length = 32)
	private String id;

	/**
	 * 用户可见 UID（纯数字，起始 1000000），用于客户端展示、客服核对、邀请等；
	 * 与内部主键 id 解耦。由 Redis INCR 分配（DB MAX(uid) 兜底），应用层在注册/建号时写入。
	 */
	@Column(name = "uid", unique = true)
	private Long uid;

	/**
	 * 邮箱（邮箱注册/登录凭证，全局唯一）
	 */
	private String email;

	/**
	 * 纯手机号（不含区号），与 phoneAreaCode 组合唯一
	 */
	private String phone;

	/**
	 * 手机区号，如 86/1
	 */
	private String phoneAreaCode;

	/**
	 * 注册时使用的联系方式类型（对应 Constant.联系方式_*）
	 */
	private String contactType;

	private String nickName;

	private String loginPwd;

	private String fundPwd;

	private String status;

	private String c2cBuyStatus;

	private String c2cSellStatus;

	private LocalDateTime createTime;

	private LocalDateTime latelyLoginTime;

	private String authenticationStatus;

	private LocalDateTime authenticationPassedTime;

	private String realName;

	private String inviteCode;

	private Integer accountLevel;

	private String accountLevelPath;

	private String googleSecretKey;

	private LocalDateTime googleAuthBindTime;

	/** 通知语言：zh-CN / zh-TW / en-US */
	@Column(name = "notify_lang_code")
	private String notifyLangCode;

	@NotFound(action = NotFoundAction.IGNORE)
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "notify_lang_code", referencedColumnName = "lang_code", updatable = false, insertable = false, foreignKey = @ForeignKey(value = ConstraintMode.NO_CONSTRAINT))
	private MultiLang notifyMultiLang;

	private Boolean deletedFlag;

	private LocalDateTime deletedTime;

	@Version
	private Long version;

	@Column(name = "inviter_id", length = 32)
	private String inviterId;

	@NotFound(action = NotFoundAction.IGNORE)
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "inviter_id", updatable = false, insertable = false, foreignKey = @ForeignKey(value = ConstraintMode.NO_CONSTRAINT))
	private MemberAccount inviter;

	@Column(name = "member_authentication_info_id", length = 32)
	private String memberAuthenticationInfoId;

	@NotFound(action = NotFoundAction.IGNORE)
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "member_authentication_info_id", updatable = false, insertable = false, foreignKey = @ForeignKey(value = ConstraintMode.NO_CONSTRAINT))
	private MemberAuthenticationInfo memberAuthenticationInfo;

	public void unBindGoogleAuth() {
		this.setGoogleSecretKey(null);
		this.setGoogleAuthBindTime(null);
	}

	public void bindGoogleAuth(String googleSecretKey) {
		this.setGoogleSecretKey(googleSecretKey);
		this.setGoogleAuthBindTime(LocalDateTime.now());
	}

	public void updateInviteInfo(MemberAccount inviter) {
		this.setInviterId(inviter.getId());
		this.setAccountLevel(inviter.getAccountLevel() + 1);
		this.setAccountLevelPath(inviter.getAccountLevelPath() + "." + this.getId());
	}

	public void validFundPwd(String pwd) {
		if (StrUtil.isBlank(this.getFundPwd())) {
			throw new BizException("请先设置资金密码");
		}
		if (!PwdUtils.pwdVerify(pwd, this.getFundPwd())) {
			throw new BizException("资金密码不正确");
		}
	}

	public static MemberAccount build(String userName, String loginPwd) {
		MemberAccount po = new MemberAccount();
		po.setId(IdUtils.getId());
		po.setNickName("m" + userName.substring(userName.length() - 4));
		po.setLoginPwd(PwdUtils.pwdEncrypt(loginPwd));
		po.setStatus(Constant.账号状态_正常);
		po.setC2cBuyStatus(Constant.启用状态_启用);
		po.setC2cSellStatus(Constant.启用状态_启用);
		po.setAuthenticationStatus(Constant.会员认证状态_未认证);
		po.setCreateTime(LocalDateTime.now());
		po.setAccountLevel(1);
		po.setAccountLevelPath(po.getId());
		po.setDeletedFlag(false);
		return po;
	}

	public void validBasicRisk() {
		if (Constant.账号状态_已注销.equals(this.getStatus())) {
			throw new BizException("账号已注销");
		}
		if (Constant.账号状态_冻结.equals(this.getStatus())) {
			throw new BizException("账号已被冻结");
		}
		if (Constant.会员认证状态_未认证.equals(this.getAuthenticationStatus())) {
			throw new BizException("账号未实名");
		}
		if (Constant.会员认证状态_认证待审核.equals(this.getAuthenticationStatus())) {
			throw new BizException("实名信息审核中");
		}
	}

	public void validSellRisk() {
		validBasicRisk();
		if (Constant.启用状态_停用.equals(this.getC2cSellStatus())) {
			throw new BizException("卖出功能已被停用");
		}
		if (this.checkRiskLimit(Constant.C2C风控处罚_限制卖出)) {
			throw new BizException("已被限制卖出");
		}
	}

	public void validBuyRisk() {
		validBasicRisk();
		if (Constant.启用状态_停用.equals(this.getC2cBuyStatus())) {
			throw new BizException("买入功能已被停用");
		}
		if (this.checkRiskLimit(Constant.C2C风控处罚_限制买入)) {
			throw new BizException("已被限制买入");
		}
	}

	public Boolean checkRiskLimit(String riskPunish) {
		String limit = RedisUtils.opsForValueGet(riskPunish + this.getId());
		return StrUtil.isNotBlank(limit);
	}

	/** 注销：状态置已注销 + 逻辑删除 */
	public void cancel() {
		this.status = Constant.账号状态_已注销;
		this.deletedFlag = true;
		this.deletedTime = LocalDateTime.now();
	}

	/** 恢复：状态置正常 + 清除删除标记 */
	public void restore() {
		this.status = Constant.账号状态_正常;
		this.deletedFlag = false;
		this.deletedTime = null;
	}

}
