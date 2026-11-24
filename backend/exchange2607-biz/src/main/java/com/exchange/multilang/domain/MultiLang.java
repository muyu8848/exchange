package com.exchange.multilang.domain;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Locale;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Version;

import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

import lombok.Getter;
import lombok.Setter;

/**
 * 多语言配置：一行代表一种受支持的语言。
 * <p>
 * 仅维护「语言清单」（启用开关 / 排序 / 展示名），文案仍存放于 messages_*.properties。
 */
@Getter
@Setter
@Entity
@Table(name = "multi_lang")
@DynamicInsert(true)
@DynamicUpdate(true)
public class MultiLang implements Serializable {

	private static final long serialVersionUID = 1L;

	@Id
	@Column(name = "id", length = 32)
	private String id;

	/** 语言码，须能被 Locale.forLanguageTag 解析：zh-CN / zh-TW / en-US */
	@Column(name = "lang_code", nullable = false, unique = true)
	private String langCode;

	/** 语言名称（中文标注，后台展示） */
	@Column(name = "lang_name")
	private String langName;

	/** 母语名称（客户端下拉框展示，不翻译） */
	@Column(name = "native_name")
	private String nativeName;

	/** 状态：Constant.启用状态_启用 / 启用状态_停用 */
	@Column(name = "status", length = 1)
	private String status;

	private Integer sortNum;

	private LocalDateTime createTime;

	private LocalDateTime updateTime;

	@Version
	private Long version;

	public Locale toLocale() {
		return Locale.forLanguageTag(this.langCode);
	}

}
