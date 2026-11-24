package com.exchange.currency.domain;

import java.io.Serializable;
import java.time.LocalDateTime;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

import lombok.Getter;
import lombok.Setter;

/**
 * 币种
 */
@Getter
@Setter
@Entity
@Table(name = "currency")
@DynamicInsert(true)
@DynamicUpdate(true)
public class Currency implements Serializable {

	/**
	 *
	 */
	private static final long serialVersionUID = 1L;

	@Id
	@Column(name = "id", length = 32)
	private String id;

	@Column(name = "currency_code")
	private String currencyCode;

	private String currencyName;

	private String currencyLogo;

	private Integer sortNum;
	
	private Integer scale;
	
	private LocalDateTime createTime;

	private LocalDateTime updateTime;

	private Boolean deletedFlag;

	private LocalDateTime deletedTime;

	public void deleted() {
		this.setDeletedFlag(true);
		this.setDeletedTime(LocalDateTime.now());
	}

}
