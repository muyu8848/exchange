package com.exchange.multilang.param;

import java.time.LocalDateTime;

import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

import org.springframework.beans.BeanUtils;

import com.exchange.common.utils.IdUtils;
import com.exchange.multilang.domain.MultiLang;

import lombok.Data;

/**
 * 多语言配置新增 / 编辑入参。
 * <p>
 * 语言编码仅在新增时生效，编辑时忽略（改编码会破坏客户端本地缓存与文案文件名）。
 */
@Data
public class AddOrUpdateMultiLangParam {

	private String id;

	/** 语言编码，须能被 Locale.forLanguageTag 解析：zh-CN / zh-TW / en-US */
	@NotBlank
	private String langCode;

	/** 语言名称（中文标注，后台展示） */
	@NotBlank
	private String langName;

	/** 母语名称（客户端下拉框展示，如「繁體中文」） */
	@NotBlank
	private String nativeName;

	/** 状态：Constant.启用状态_启用 / 启用状态_停用 */
	@NotBlank
	private String status;

	@NotNull
	@DecimalMin(value = "0", inclusive = true)
	private Integer sortNum;

	public MultiLang convertToPo() {
		MultiLang po = new MultiLang();
		BeanUtils.copyProperties(this, po);
		po.setId(IdUtils.getId());
		po.setCreateTime(LocalDateTime.now());
		po.setUpdateTime(po.getCreateTime());
		return po;
	}

}
