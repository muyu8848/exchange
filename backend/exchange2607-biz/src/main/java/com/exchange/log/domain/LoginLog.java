package com.exchange.log.domain;

import java.io.Serializable;
import java.time.LocalDateTime;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

import com.exchange.common.utils.IdUtils;
import com.exchange.constants.Constant;

import cn.hutool.http.useragent.UserAgent;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "login_log")
@DynamicInsert(true)
@DynamicUpdate(true)
public class LoginLog implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	@Id
	@Column(name = "id", length = 32)
	private String id;

	private String subSystem;

	private String status;

	private String ip;

	private LocalDateTime loginTime;

	private String browser;

	private String os;

	private String msg;

	private String userName;

	/**
	 * 登录账号 ID（后台管理员 ID 或客户端会员 ID）。 账号不存在时为 null。
	 */
	private String accountId;

	private String token;

	public LoginLog loginFail(String accountId, String msg) {
		this.setStatus(Constant.登录状态_失败);
		this.setAccountId(accountId);
		this.setMsg(msg);
		return this;
	}

	public LoginLog loginSuccess(String accountId, String token) {
		this.setStatus(Constant.登录状态_成功);
		this.setMsg(Constant.登录提示_登录成功);
		this.setAccountId(accountId);
		this.setToken(token);
		return this;
	}

	public static LoginLog buildLog(String userName, String subSystem, String ip, UserAgent userAgent) {
		LoginLog loginLog = new LoginLog();
		loginLog.setId(IdUtils.getId());
		loginLog.setUserName(userName);
		loginLog.setSubSystem(subSystem);
		loginLog.setIp(ip);
		loginLog.setLoginTime(LocalDateTime.now());
		loginLog.setBrowser(userAgent.getBrowser().getName());
		loginLog.setOs(userAgent.getOs().getName());
		return loginLog;
	}

	/**
	 * 构建登录日志（带 accountId）。
	 *
	 * @param accountId 登录账号 ID（后台管理员 ID 或客户端会员 ID），账号不存在时传 null
	 * @param userName  登录时使用的标识（后台=管理员账号名，客户端=邮箱/手机号原文）
	 */
	public static LoginLog buildLog(String accountId, String userName, String subSystem, String ip,
			UserAgent userAgent) {
		LoginLog loginLog = buildLog(userName, subSystem, ip, userAgent);
		loginLog.setAccountId(accountId);
		return loginLog;
	}

}
