package com.exchange.contract.domain;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "perpetual_funding_rate_history")
@DynamicInsert(true)
@DynamicUpdate(true)
public class PerpetualFundingRateHistory implements Serializable {

	private static final long serialVersionUID = 1L;

	@Id
	@Column(name = "id", length = 32)
	private String id;

	@Column(name = "symbol_code", length = 32)
	private String symbolCode;

	@Column(name = "funding_rate", precision = 18, scale = 8)
	private BigDecimal fundingRate;

	@Column(name = "funding_time")
	private LocalDateTime fundingTime;

	@Column(name = "create_time")
	private LocalDateTime createTime;

}
