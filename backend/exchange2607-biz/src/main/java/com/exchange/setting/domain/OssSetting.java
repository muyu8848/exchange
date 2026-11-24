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
import com.exchange.constants.Constant;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "oss_setting")
@DynamicInsert(true)
@DynamicUpdate(true)
public class OssSetting implements Serializable {

	private static final long serialVersionUID = 1L;

	@Id
	@Column(name = "id", length = 32)
	private String id;

	/** @see Constant#存储类型_本地 */
	private String type;

	private String localStoragePath;

	private String publicFetchUrl;

	private LocalDateTime latelyUpdateTime;

	public static OssSetting build() {
		OssSetting setting = new OssSetting();
		setting.setId(IdUtils.getId());
		setting.setType(Constant.存储类型_本地);
		return setting;
	}

}
