package com.exchange.deposit.domain;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

import javax.persistence.Column;
import javax.persistence.ConstraintMode;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.ForeignKey;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;
import org.hibernate.annotations.NotFound;
import org.hibernate.annotations.NotFoundAction;

import com.exchange.constants.Constant;
import com.exchange.currency.domain.Currency;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "deposit_channel")
@DynamicInsert(true)
@DynamicUpdate(true)
public class DepositChannel implements Serializable {

	private static final long serialVersionUID = 1L;

	@Id
	@Column(name = "id", length = 32)
	private String id;

	@Column(name = "chain_type")
	private String chainType;

	@Column(name = "order_expire_minutes")
	private Integer orderExpireMinutes;

	@Column(precision = Constant.数值总位数, scale = Constant.小数部分位数)
	private BigDecimal minAmount;

	@Column(precision = Constant.数值总位数, scale = Constant.小数部分位数)
	private BigDecimal maxAmount;

	@Column(name = "sort_num")
	private Integer sortNum;

	@Column(name = "asset_code")
	private String assetCode;

	@NotFound(action = NotFoundAction.IGNORE)
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "asset_code", referencedColumnName = "currency_code", updatable = false, insertable = false, foreignKey = @ForeignKey(value = ConstraintMode.NO_CONSTRAINT))
	private Currency currency;

	private String status;

	private LocalDateTime createTime;

	private LocalDateTime updateTime;

	private Boolean deletedFlag;

	private LocalDateTime deletedTime;

	public void deleted() {
		this.setDeletedFlag(true);
		this.setDeletedTime(LocalDateTime.now());
	}

}
