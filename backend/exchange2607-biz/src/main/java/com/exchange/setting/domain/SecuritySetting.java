package com.exchange.setting.domain;

import java.io.Serializable;
import java.time.LocalDateTime;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

import com.exchange.common.utils.IdUtils;

import lombok.Getter;
import lombok.Setter;

/**
 * 安全设置
 */
@Getter
@Setter
@Entity
@Table(name = "security_setting")
@DynamicInsert(true)
@DynamicUpdate(true)
public class SecuritySetting implements Serializable {

	private static final long serialVersionUID = 1L;

	@Id
	@Column(name = "id", length = 32)
	private String id;

	/** 会员最大在线端数 */
	private Integer memberMaxLoginCount;

	/** 账号连续登录失败上限 */
	private Integer accountLoginFailMaxAttempts;

	/** 账号登录锁定时长（分钟） */
	private Integer accountLoginFailLockMinutes;

	/** 同一IP连续登录失败上限 */
	private Integer ipLoginFailMaxAttempts;

	/** IP登录锁定时长（分钟） */
	private Integer ipLoginFailLockMinutes;

	private LocalDateTime latelyUpdateTime;

	public static SecuritySetting build() {
		SecuritySetting setting = new SecuritySetting();
		setting.setId(IdUtils.getId());
		return setting;
	}
}
