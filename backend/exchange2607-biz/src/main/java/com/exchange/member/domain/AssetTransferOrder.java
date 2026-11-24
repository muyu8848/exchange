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

import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;
import org.hibernate.annotations.NotFound;
import org.hibernate.annotations.NotFoundAction;

import com.exchange.common.utils.IdUtils;
import com.exchange.constants.Constant;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "asset_transfer_order")
@DynamicInsert(true)
@DynamicUpdate(true)
public class AssetTransferOrder implements Serializable {

	private static final long serialVersionUID = 1L;

	@Id
	@Column(name = "id", length = 32)
	private String id;

	private String orderNo;

	/** 划出账户类型 SPOT / FUTURES */
	private String fromAccountType;

	/** 划入账户类型 SPOT / FUTURES */
	private String toAccountType;

	private String assetCode;

	@Column(precision = Constant.数值总位数, scale = Constant.小数部分位数)
	private BigDecimal amount;

	private LocalDateTime createTime;
	
	@Column(name = "member_account_id", length = 32)
	private String memberAccountId;

	@NotFound(action = NotFoundAction.IGNORE)
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "member_account_id", updatable = false, insertable = false, foreignKey = @ForeignKey(value = ConstraintMode.NO_CONSTRAINT))
	private MemberAccount memberAccount;

	public static AssetTransferOrder build(String memberAccountId, String fromAccountType, String toAccountType,
			String assetCode, BigDecimal amount) {
		AssetTransferOrder po = new AssetTransferOrder();
		po.setId(IdUtils.getId());
		po.setOrderNo(po.getId());
		po.setMemberAccountId(memberAccountId);
		po.setFromAccountType(fromAccountType);
		po.setToAccountType(toAccountType);
		po.setAssetCode(assetCode);
		po.setAmount(amount);
		po.setCreateTime(LocalDateTime.now());
		return po;
	}

}
