package com.exchange.member.domain;

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
import javax.persistence.Version;

import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;
import org.hibernate.annotations.NotFound;
import org.hibernate.annotations.NotFoundAction;

import com.exchange.constants.Constant;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "member_asset")
@DynamicInsert(true)
@DynamicUpdate(true)
public class MemberAsset implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	@Id
	@Column(name = "id")
	private String id;

	/** 账户类型：现货 SPOT / 合约 FUTURES / 法币 FIAT（C2C 资产账户），见 Constant.账户类型_* */
	private String accountType;

	/** 资产代码：USDT / BTC / ETH ，见 Constant.资产_* */
	private String assetCode;

	@Column(precision = Constant.数值总位数, scale = Constant.小数部分位数)
	private BigDecimal available = BigDecimal.ZERO;

	@Column(precision = Constant.数值总位数, scale = Constant.小数部分位数)
	private BigDecimal frozen = BigDecimal.ZERO;

	@Version
	private Long version;

	@Column(name = "member_account_id", nullable = false)
	private String memberAccountId;

	/** 创建时间 */
	private LocalDateTime createTime;

	@NotFound(action = NotFoundAction.IGNORE)
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "member_account_id", updatable = false, insertable = false, foreignKey = @ForeignKey(value = ConstraintMode.NO_CONSTRAINT))
	private MemberAccount memberAccount;

}
