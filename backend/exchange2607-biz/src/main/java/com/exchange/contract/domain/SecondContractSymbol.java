package com.exchange.contract.domain;

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

import com.exchange.currency.domain.Currency;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "second_contract_symbol")
@DynamicInsert(true)
@DynamicUpdate(true)
public class SecondContractSymbol implements Serializable {

	private static final long serialVersionUID = 1L;

	@Id
	@Column(name = "id", length = 32)
	private String id;

	@Column(name = "symbol_code", length = 32)
	private String symbolCode;

	@Column(name = "base_currency_code", length = 16)
	private String baseCurrencyCode;

	@Column(name = "quote_currency_code", length = 16)
	private String quoteCurrencyCode;

	@NotFound(action = NotFoundAction.IGNORE)
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "base_currency_code", updatable = false, insertable = false, foreignKey = @ForeignKey(value = ConstraintMode.NO_CONSTRAINT))
	private Currency baseCurrency;

	@NotFound(action = NotFoundAction.IGNORE)
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "quote_currency_code", updatable = false, insertable = false, foreignKey = @ForeignKey(value = ConstraintMode.NO_CONSTRAINT))
	private Currency quoteCurrency;

	private String status;

	@Column(name = "tick_size", precision = 18, scale = 8)
	private BigDecimal tickSize;

	private Integer sortNum;

	@Column(name = "price_scale")
	private Integer priceScale;

	private LocalDateTime createTime;

	private LocalDateTime updateTime;

	@Column(name = "deleted_flag")
	private Boolean deletedFlag;

	@Column(name = "deleted_time")
	private LocalDateTime deletedTime;

	public void deleted() {
		this.setDeletedFlag(true);
		this.setDeletedTime(LocalDateTime.now());
	}

}
