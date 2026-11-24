package com.exchange.background.domain;

import java.io.Serializable;
import java.time.LocalDateTime;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Version;

import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

import com.exchange.common.exception.BizException;
import com.exchange.common.utils.IdUtils;
import com.exchange.constants.Constant;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "background_account")
@DynamicInsert(true)
@DynamicUpdate(true)
public class BackgroundAccount implements Serializable {

	private static final long serialVersionUID = 1L;

	@Id
	@Column(name = "id", length = 32)
	private String id;

	private String userName;

	private String loginPwd;

	private Boolean superAdminFlag;

	private String status;

	private LocalDateTime createTime;

	private LocalDateTime latelyLoginTime;

	private Boolean deletedFlag;

	private LocalDateTime deletedTime;

	private String googleSecretKey;

	private LocalDateTime googleAuthBindTime;
	
	@Version
	private Long version;

	public static BackgroundAccount build(String userName, String loginPwd) {
		BackgroundAccount po = new BackgroundAccount();
		po.setId(IdUtils.getId());
		po.setStatus(Constant.启用状态_启用);
		po.setDeletedFlag(false);
		po.setCreateTime(LocalDateTime.now());
		po.setSuperAdminFlag(false);
		po.setUserName(userName);
		po.setLoginPwd(loginPwd);
		return po;
	}

	public void validSuperAdmin() {
		if (!this.getSuperAdminFlag()) {
			throw new BizException("非超级管理员,无权操作");
		}
	}

	public void deleted() {
		this.setDeletedFlag(true);
		this.setDeletedTime(LocalDateTime.now());
	}

	public void unBindGoogleAuth() {
		this.setGoogleSecretKey(null);
		this.setGoogleAuthBindTime(null);
	}

	public void bindGoogleAuth(String googleSecretKey) {
		this.setGoogleSecretKey(googleSecretKey);
		this.setGoogleAuthBindTime(LocalDateTime.now());
	}
}
