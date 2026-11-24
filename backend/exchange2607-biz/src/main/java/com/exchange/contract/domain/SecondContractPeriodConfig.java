package com.exchange.contract.domain;

import java.io.Serializable;
import java.math.BigDecimal;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;
import org.springframework.beans.BeanUtils;

import com.exchange.common.utils.IdUtils;
import com.exchange.constants.Constant;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "second_contract_period_config")
@DynamicInsert(true)
@DynamicUpdate(true)
public class SecondContractPeriodConfig implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	@Id
	@Column(name = "id", length = 32)
	private String id;

	private Integer period;

	@Column(precision = Constant.数值总位数, scale = Constant.小数部分位数)
	private BigDecimal minAmount;

	@Column(precision = Constant.数值总位数, scale = Constant.小数部分位数)
	private BigDecimal maxAmount;

	@Column(precision = Constant.数值总位数, scale = Constant.小数部分位数)
	private BigDecimal odds;

	private String symbolCode;

	public SecondContractPeriodConfig copy(String target) {
		SecondContractPeriodConfig po = new SecondContractPeriodConfig();
		BeanUtils.copyProperties(this, po);
		po.setId(IdUtils.getId());
		po.setSymbolCode(target);
		return po;
	}

}
