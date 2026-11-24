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

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "system_setting")
@DynamicInsert(true)
@DynamicUpdate(true)
public class SystemSetting implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	@Id
	@Column(name = "id", length = 32)
	private String id;

	private String appDownloadUrl;

	private String appSchema;

	private String h5Url;

	private String customerServiceUrl;

	private LocalDateTime latelyUpdateTime;

	public static SystemSetting build() {
		SystemSetting setting = new SystemSetting();
		setting.setId(IdUtils.getId());
		return setting;
	}

}
