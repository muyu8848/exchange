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

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "rebate_setting")
@DynamicInsert(true)
@DynamicUpdate(true)
public class RebateSetting implements Serializable {

	private static final long serialVersionUID = 1L;

	@Id
	@Column(name = "id", length = 32)
	private String id;

	/** 直推返佣比例（手续费 × 该比例发放给推荐人），0~1 */
	@Column(precision = 10, scale = 6)
	private BigDecimal rate;

	private LocalDateTime latelyUpdateTime;

	public static RebateSetting build() {
		RebateSetting setting = new RebateSetting();
		setting.setId(IdUtils.getId());
		return setting;
	}
}
