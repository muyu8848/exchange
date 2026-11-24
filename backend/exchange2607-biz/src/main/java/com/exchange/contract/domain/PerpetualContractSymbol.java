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

import com.exchange.constants.Constant;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "perpetual_contract_symbol")
@DynamicInsert(true)
@DynamicUpdate(true)
public class PerpetualContractSymbol implements Serializable {

	private static final long serialVersionUID = 1L;

	@Id
	@Column(name = "id", length = 32)
	private String id;

	@Column(name = "symbol_code")
	private String symbolCode;

	private String baseCurrencyCode;

	private String quoteCurrencyCode;

	private Integer maxLeverage;

	private Integer defaultLeverage;

	@Column(precision = Constant.数值总位数, scale = Constant.小数部分位数)
	private BigDecimal maintenanceMarginRate;

	@Column(precision = Constant.数值总位数, scale = Constant.小数部分位数)
	private BigDecimal takerFeeRate;

	private Integer fundingInterval;

	@Column(precision = Constant.数值总位数, scale = Constant.小数部分位数)
	private BigDecimal defaultFundingRate;

	private Integer priceScale;

	private Integer amountScale;

	@Column(precision = Constant.数值总位数, scale = Constant.小数部分位数)
	private BigDecimal minOrderValue;

	private String status;

	private Integer sortNum;

	private Boolean deletedFlag;

	private LocalDateTime deletedTime;

	private LocalDateTime createTime;

	private LocalDateTime updateTime;

	public void deleted() {
		this.setDeletedFlag(true);
		this.setDeletedTime(LocalDateTime.now());
	}

}
