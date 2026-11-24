package com.exchange.setting.domain;

import java.io.Serializable;
import java.math.BigDecimal;
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
 * 闪兑设置
 */
@Getter
@Setter
@Entity
@Table(name = "convert_setting")
@DynamicInsert(true)
@DynamicUpdate(true)
public class ConvertSetting implements Serializable {

	private static final long serialVersionUID = 1L;

	@Id
	@Column(name = "id", length = 32)
	private String id;

	/** 是否启用 */
	@Column(name = "enabled")
	private Boolean enabled;

	/** 全局点差（如 0.003 = 0.3%） */
	@Column(name = "spread", precision = 10, scale = 8)
	private BigDecimal spread;

	/** 最小兑换金额（USDT 等值） */
	@Column(name = "min_amount", precision = 20, scale = 8)
	private BigDecimal minAmount;

	/** 最大兑换金额（USDT 等值） */
	@Column(name = "max_amount", precision = 20, scale = 8)
	private BigDecimal maxAmount;

	/** 报价锁定时长（秒），默认15 */
	@Column(name = "quote_ttl_seconds")
	private Integer quoteTtlSeconds;

	private LocalDateTime latelyUpdateTime;

	public static ConvertSetting build() {
		ConvertSetting setting = new ConvertSetting();
		setting.setId(IdUtils.getId());
		setting.setEnabled(true);
		setting.setSpread(new BigDecimal("0.003"));
		setting.setMinAmount(new BigDecimal("10"));
		setting.setMaxAmount(new BigDecimal("50000"));
		setting.setQuoteTtlSeconds(15);
		return setting;
	}
}
