package com.exchange.setting.domain;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

import com.exchange.common.utils.IdUtils;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "mail_setting")
@DynamicInsert(true)
@DynamicUpdate(true)
public class MailSetting implements Serializable {

	/**
	 *
	 */
	private static final long serialVersionUID = 1L;

	@Id
	@Column(name = "id", length = 32)
	private String id;

	private String host;
	
	private Integer port;
	
	private Boolean needAuthFlag;
	
	private String mailAccount;
	
	private String mailUser;
	
	private String mailPwd;

	/** 加密方式：SSL(465) / STARTTLS(587) / NONE */
	private String encryptionType;

	private Date latelyUpdateTime;
	
	public static MailSetting build() {
		MailSetting setting = new MailSetting();
		setting.setId(IdUtils.getId());
		return setting;
	}

}
